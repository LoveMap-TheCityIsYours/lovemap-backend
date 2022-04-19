package com.smackmap.smackmapbackend.smack.location

import com.smackmap.smackmapbackend.relation.RelationService
import com.smackmap.smackmapbackend.security.SmackerAuthorizationService
import com.smackmap.smackmapbackend.smack.*
import com.smackmap.smackmapbackend.smack.location.review.SmackLocationReview
import com.smackmap.smackmapbackend.smack.location.review.SmackLocationReviewDto
import com.smackmap.smackmapbackend.smack.location.review.SmackLocationReviewService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class SmackService(
    private val authorizationService: SmackerAuthorizationService,
    private val relationService: RelationService,
    private val locationService: SmackLocationService,
    private val reviewService: SmackLocationReviewService,
    private val smackRepository: SmackRepository
) {
    suspend fun list(smackerId: Long): SmackListDto {
        authorizationService.checkAccessFor(smackerId)
        val smacks = smackRepository
            .findDistinctBySmackerIdOrSmackerPartnerId(smackerId, smackerId)
        val locationIds: Flow<Long> = smacks.map { it.smackLocationId }
        val locations: Flow<SmackLocation> = locationService.findAllByIds(locationIds)
        val reviews: Flow<SmackLocationReview> = reviewService.findAllByLocationIdIn(locationIds)
        return SmackListDto(
            smacks = smacks.map { SmackDto.of(it) },
            smackLocations = locations.map { SmackLocationDto.of(it) },
            smackLocationReviews = reviews.map { SmackLocationReviewDto.of(it) }
        )
    }

    suspend fun create(request: CreateSmackRequest): Smack {
        authorizationService.checkAccessFor(request.smackerId)
        locationService.checkExistence(request.smackLocationId)
        request.smackerPartnerId?.let {
            relationService.checkPartnership(request.smackerId, it)
        }
        return smackRepository.save(
            Smack(
                name = request.name,
                smackLocationId = request.smackLocationId,
                smackerId = request.smackerId,
                smackerPartnerId = request.smackerPartnerId
            )
        )
    }
}