package com.lovemap.lovemapbackend.lover.relation

import com.lovemap.lovemapbackend.authentication.security.AuthorizationService
import com.lovemap.lovemapbackend.lover.*
import com.lovemap.lovemapbackend.lover.ranking.LoverPointService
import com.lovemap.lovemapbackend.lover.relation.Relation.Status.FOLLOWING
import com.lovemap.lovemapbackend.lover.relation.Relation.Status.PARTNER
import com.lovemap.lovemapbackend.newsfeed.NewsFeedService
import com.lovemap.lovemapbackend.newsfeed.api.NewsFeedItemResponse
import com.lovemap.lovemapbackend.notification.NotificationService
import com.lovemap.lovemapbackend.utils.ErrorCode
import com.lovemap.lovemapbackend.utils.ErrorMessage
import com.lovemap.lovemapbackend.utils.LoveMapException
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.flow.toSet
import mu.KotlinLogging
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class RelationService(
    private val authorizationService: AuthorizationService,
    private val loverService: LoverService,
    private val loverConverter: LoverConverter,
    private val cachedLoverService: CachedLoverService,
    private val loverPointService: LoverPointService,
    private val newsFeedService: NewsFeedService,
    private val notificationService: NotificationService,
    private val repository: RelationRepository,
) {
    private val logger = KotlinLogging.logger {}

    suspend fun checkBlockingBetweenLovers(initiatorId: Long, respondentId: Long) {
        if (repository.existsBySourceIdAndTargetIdAndStatus(
                initiatorId,
                respondentId,
                Relation.Status.BLOCKED
            )
        ) {
            logger.info { "User '$initiatorId' blocked '$respondentId'" }
        }
        if (repository.existsBySourceIdAndTargetIdAndStatus(
                respondentId,
                initiatorId,
                Relation.Status.BLOCKED
            ) && !authorizationService.isAdmin()
        ) {
            throw LoveMapException(
                HttpStatus.FORBIDDEN,
                ErrorMessage(
                    ErrorCode.BlockedByUser,
                    respondentId.toString(),
                    "User '$initiatorId' is blocked by '$respondentId'."
                )
            )
        }
    }

    @Transactional
    suspend fun setPartnershipBetween(user1: Long, user2: Long) {
        if (user1 == user2) {
            throw LoveMapException(
                HttpStatus.BAD_REQUEST,
                ErrorMessage(
                    ErrorCode.BadRequest,
                    user1.toString(),
                    "Source and Target in a relation cannot be the same. '$user1'"
                )
            )
        }
        val relation12: Relation = repository.findBySourceIdAndTargetId(user1, user2)
            ?: Relation(status = PARTNER, sourceId = user1, targetId = user2)
        relation12.status = PARTNER
        repository.save(relation12)

        val relation21: Relation = repository.findBySourceIdAndTargetId(user2, user1)
            ?: Relation(status = PARTNER, sourceId = user2, targetId = user1)
        relation21.status = PARTNER
        repository.save(relation21)
    }

    suspend fun removePartnershipBetween(loverId: Long, partnerLoverId: Long) {
        repository.deleteBySourceIdAndTargetId(loverId, partnerLoverId)
        repository.deleteBySourceIdAndTargetId(partnerLoverId, loverId)
    }

    suspend fun getRelation(fromId: Long, toId: Long): Relation {
        return repository.findBySourceIdAndTargetId(fromId, toId)
            ?: throw LoveMapException(
                HttpStatus.NOT_FOUND,
                ErrorMessage(
                    ErrorCode.RelationNotFound,
                    toId.toString(),
                    "Relation not found from '$fromId' to '$toId'."
                )
            )
    }

    suspend fun arePartners(fromId: Long, toId: Long): Boolean {
        return repository.existsBySourceIdAndTargetIdAndStatus(fromId, toId, PARTNER)
    }

    suspend fun getRelationStatusDto(fromId: Long, toId: Long): RelationStatusDto {
        return RelationStatusDto.of(repository.findBySourceIdAndTargetId(fromId, toId)?.status)
    }

    suspend fun getRelationsFrom(fromId: Long): LoverRelations {
        val relationFlow = repository.findBySourceIdAndStatusIn(fromId, setOf(FOLLOWING, PARTNER))
        val loverRelationFlow = relationFlow.map { value: Relation ->
            val inRelationWith = loverService.unAuthorizedGetById(value.targetId)
            LoverRelation(
                inRelationWith.toView(),
                inRelationWith.rank,
                value.status
            )
        }
        return LoverRelations(fromId, loverRelationFlow)
    }

    suspend fun checkPartnership(loverId: Long, partnerId: Long) {
        checkBlockingBetweenLovers(loverId, partnerId)
        if (!repository.existsBySourceIdAndTargetIdAndStatus(loverId, partnerId, PARTNER)) {
            throw LoveMapException(
                HttpStatus.NOT_FOUND,
                ErrorMessage(
                    ErrorCode.PartnershipNotFound,
                    partnerId.toString(),
                    "No partnership between '$loverId' and '$partnerId'"
                )
            )
        }
    }

    suspend fun getFollowingNewsFeed(loverId: Long): List<NewsFeedItemResponse> {
        authorizationService.checkAccessFor(loverId)
        val followedLoverIds = repository.findBySourceIdAndStatus(loverId, FOLLOWING)
            .map { it.targetId }
            .toSet()

        if (followedLoverIds.isEmpty()) {
            return emptyList()
        }
        return newsFeedService.getActivitiesOfLovers(followedLoverIds)
    }

    @Transactional
    suspend fun followLover(loverId: Long, targetLoverId: Long): LoverRelationsResponse {
        val caller = authorizationService.checkAccessFor(loverId)
        checkBlockingBetweenLovers(loverId, targetLoverId)
        val targetLover = checkPublicProfile(targetLoverId)

        repository.findBySourceIdAndTargetId(loverId, targetLoverId)?.let { currentRelation ->
            logger.info {
                "Lover '$loverId' tried to follow '$targetLoverId' " +
                        "but they already have a relation: '${currentRelation.status}'"
            }
        } ?: run {
            logger.info { "Setting '$loverId' as follower of '$targetLoverId'" }
            repository.save(
                Relation(
                    status = FOLLOWING,
                    sourceId = loverId,
                    targetId = targetLoverId
                )
            )
            loverPointService.incrementFollowers(caller, targetLover)
        }

        notificationService.sendNewFollowerNotification(targetLover)

        val lover = loverService.getById(loverId)
        return loverConverter.toRelationsResponse(lover, getRelationsFrom(loverId))
    }

    @Transactional
    suspend fun unfollowLover(loverId: Long, targetLoverId: Long): LoverRelationsResponse {
        val caller = authorizationService.checkAccessFor(loverId)
        repository.findBySourceIdAndTargetId(loverId, targetLoverId)?.let { currentRelation ->
            if (currentRelation.status == FOLLOWING) {
                logger.info { "'$loverId' Unfollowed '$targetLoverId'" }
                repository.delete(currentRelation)
                loverPointService.decrementFollowers(caller, targetLoverId)
            } else {
                logger.info {
                    "Lover '$loverId' tried to unfollow '$targetLoverId' " +
                            "but they already have a relation: '${currentRelation.status}'"
                }
            }
        } ?: run {
            logger.info {
                "Lover '$loverId' tried to unfollow '$targetLoverId' but they don't have a relation"
            }
        }
        val lover = loverService.getById(loverId)
        return loverConverter.toRelationsResponse(lover, getRelationsFrom(loverId))
    }

    suspend fun getFollowers(targetLoverId: Long): List<LoverViewWithoutRelationResponse> {
        val caller = authorizationService.getCaller()
        checkBlockingBetweenLovers(caller.id, targetLoverId)
        if (canAccessFollowers(targetLoverId, caller)) {
            return doListFollowers(targetLoverId)
        } else {
            throw LoveMapException(HttpStatus.UNAUTHORIZED, ErrorCode.LoverIsNotPublic)
        }
    }

    suspend fun getFollowings(sourceLoverId: Long): List<LoverViewWithoutRelationResponse> {
        val caller = authorizationService.getCaller()
        checkBlockingBetweenLovers(caller.id, sourceLoverId)
        if (canAccessFollowers(sourceLoverId, caller)) {
            return doListFollowings(sourceLoverId)
        } else {
            throw LoveMapException(HttpStatus.UNAUTHORIZED, ErrorCode.LoverIsNotPublic)
        }
    }

    @Transactional
    suspend fun removeFollower(loverId: Long, followerId: Long): List<LoverViewWithoutRelationResponse> {
        val targetLover = authorizationService.checkAccessFor(loverId)
        repository.findBySourceIdAndTargetId(followerId, loverId)?.let { currentRelation ->
            if (currentRelation.status == FOLLOWING) {
                logger.info { "Removing follower '$followerId' of '$loverId'" }
                repository.delete(currentRelation)
                loverPointService.decrementFollowers(followerId, targetLover)
                currentRelation
            } else {
                null
            }
        } ?: {
            throw LoveMapException(HttpStatus.BAD_REQUEST, ErrorCode.LoverNotFound)
        }
        return doListFollowers(loverId)
    }

    private suspend fun checkPublicProfile(targetLoverId: Long): Lover {
        val targetLover = loverService.unAuthorizedGetById(targetLoverId)
        if (!targetLover.publicProfile) {
            throw LoveMapException(HttpStatus.BAD_REQUEST, ErrorCode.LoverIsNotPublic)
        }
        return targetLover
    }

    private suspend fun isPublicProfile(targetLoverId: Long): Boolean {
        return loverService.unAuthorizedGetById(targetLoverId).publicProfile
    }

    private suspend fun canAccessFollowers(
        targetLoverId: Long,
        caller: Lover
    ) = isPublicProfile(targetLoverId) ||
            arePartners(caller.id, targetLoverId) ||
            authorizationService.isAdmin() ||
            caller.id == targetLoverId

    private suspend fun doListFollowers(targetLoverId: Long) =
        repository.findByTargetIdAndStatusOrderByCreatedAtDesc(targetLoverId, FOLLOWING)
            .mapNotNull { cachedLoverService.getCachedLoverById(it.sourceId) }
            .toList()

    private suspend fun doListFollowings(sourceLoverId: Long) =
        repository.findBySourceIdAndStatusOrderByCreatedAtDesc(sourceLoverId, FOLLOWING)
            .mapNotNull { cachedLoverService.getCachedLoverById(it.targetId) }
            .toList()
}