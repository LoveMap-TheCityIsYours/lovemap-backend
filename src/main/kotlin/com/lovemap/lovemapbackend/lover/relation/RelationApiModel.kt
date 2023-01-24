package com.lovemap.lovemapbackend.lover.relation

import com.lovemap.lovemapbackend.lover.Lover
import com.lovemap.lovemapbackend.lover.LoverService
import com.lovemap.lovemapbackend.lover.LoverViewResponse
import com.lovemap.lovemapbackend.utils.InstantConverterUtils.toApiString
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList

data class LoverRelationsResponse(
    val id: Long,
    val relations: List<LoverViewResponse>,
    val userName: String,
    val displayName: String,
    val email: String,
    val rank: Int,
    val points: Int,
    val numberOfLoves: Int,
    val reviewsSubmitted: Int,
    val reportsSubmitted: Int,
    val reportsReceived: Int,
    val loveSpotsAdded: Int,
    val numberOfFollowers: Int,
    val numberOfFollowings: Int,
    val photosUploaded: Int,
    val photoLikesReceived: Int,
    val hallOfFamePosition: Int?,
    val createdAt: String,
    val publicProfile: Boolean,
    val shareableLink: String? = null,
    val isAdmin: Boolean = false,
    val partnerId: Long?
) {
    companion object {
        suspend fun of(lover: Lover, loverRelations: LoverRelations, isAdmin: Boolean = false): LoverRelationsResponse {
            return LoverRelationsResponse(
                id = lover.id,
                userName = lover.userName,
                displayName = lover.displayName,
                email = lover.email,
                rank = lover.rank,
                points = lover.points,
                numberOfLoves = lover.numberOfLoves,
                reviewsSubmitted = lover.reviewsSubmitted,
                reportsSubmitted = lover.reportsSubmitted,
                reportsReceived = lover.reportsReceived,
                loveSpotsAdded = lover.loveSpotsAdded,
                numberOfFollowers = lover.numberOfFollowers,
                numberOfFollowings = lover.numberOfFollowings,
                photosUploaded = lover.photosUploaded,
                photoLikesReceived = lover.photoLikesReceived,
                hallOfFamePosition = lover.hallOfFamePosition,
                createdAt = lover.createdAt.toInstant().toApiString(),
                publicProfile = lover.publicProfile,
                shareableLink = lover.uuid?.let { LoverService.linkPrefixVisible + lover.uuid },
                isAdmin = isAdmin,
                partnerId = lover.partnerId,
                relations = loverRelations.relations.map { entry ->
                    LoverViewResponse(
                        id = entry.loverView.id,
                        userName = entry.loverView.displayName,
                        displayName = entry.loverView.displayName,
                        points = lover.points,
                        rank = entry.rank,
                        numberOfFollowers = entry.loverView.numberOfFollowers,
                        numberOfFollowings = entry.loverView.numberOfFollowings,
                        hallOfFamePosition = entry.loverView.hallOfFamePosition,
                        createdAt = entry.loverView.createdAt.toInstant().toApiString(),
                        relation = RelationStatusDto.of(entry.relationStatus),
                        publicProfile = false,
                        partnerId = entry.loverView.partnerId
                    )
                }.toList()
            )
        }
    }
}

data class RelationDto(
    var sourceId: Long,
    var targetId: Long,
    var status: RelationStatusDto,
) {
    companion object {
        fun of(relation: Relation): RelationDto {
            return RelationDto(
                sourceId = relation.sourceId,
                targetId = relation.targetId,
                status = RelationStatusDto.of(relation.status)
            )
        }
    }
}

enum class RelationStatusDto {
    PARTNER, FOLLOWING, BLOCKED, NOTHING;

    companion object {
        fun of(relationStatus: Relation.Status?): RelationStatusDto {
            return when (relationStatus) {
                Relation.Status.FOLLOWING -> FOLLOWING
                Relation.Status.PARTNER -> PARTNER
                Relation.Status.BLOCKED -> BLOCKED
                null -> NOTHING
            }
        }
    }
}