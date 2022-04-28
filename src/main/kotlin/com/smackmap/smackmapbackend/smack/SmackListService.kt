package com.smackmap.smackmapbackend.smack

import com.smackmap.smackmapbackend.security.AuthorizationService
import com.smackmap.smackmapbackend.smackspot.SmackSpot
import com.smackmap.smackmapbackend.smackspot.SmackSpotDto
import com.smackmap.smackmapbackend.smackspot.SmackSpotService
import com.smackmap.smackmapbackend.smackspot.review.SmackSpotReview
import com.smackmap.smackmapbackend.smackspot.review.SmackSpotReviewDto
import com.smackmap.smackmapbackend.smackspot.review.SmackSpotReviewService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class SmackListService(
    private val smackService: SmackService,
    private val authorizationService: AuthorizationService,
    private val spotService: SmackSpotService,
    private val reviewService: SmackSpotReviewService,
) {
    suspend fun list(smackerId: Long): SmackListDto {
        authorizationService.checkAccessFor(smackerId)
        val reviews: Flow<SmackSpotReview> = reviewService.findAllByReviewerId(smackerId)
        val smacks = smackService.findAllInvolvedSmacksFor(smackerId).toList()
        val locationIds: List<Long> = smacks.map { it.smackSpotId }
        val locations: Flow<SmackSpot> = spotService.findAllByIds(locationIds)
        return SmackListDto(
            smacks = smacks.map { SmackDto.of(it) },
            smackSpots = locations.map { SmackSpotDto.of(it) }.toList(),
            smackSpotReviews = reviews.map { SmackSpotReviewDto.of(it) }.toList()
        )
    }
}