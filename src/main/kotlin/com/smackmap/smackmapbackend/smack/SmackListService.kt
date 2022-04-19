package com.smackmap.smackmapbackend.smack

import com.smackmap.smackmapbackend.security.AuthorizationService
import com.smackmap.smackmapbackend.smack.location.SmackLocation
import com.smackmap.smackmapbackend.smack.location.SmackLocationDto
import com.smackmap.smackmapbackend.smack.location.SmackLocationService
import com.smackmap.smackmapbackend.smack.location.review.SmackLocationReview
import com.smackmap.smackmapbackend.smack.location.review.SmackLocationReviewDto
import com.smackmap.smackmapbackend.smack.location.review.SmackLocationReviewService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class SmackListService(
    private val smackService: SmackService,
    private val authorizationService: AuthorizationService,
    private val locationService: SmackLocationService,
    private val reviewService: SmackLocationReviewService,
) {
    suspend fun list(smackerId: Long): SmackListDto {
        authorizationService.checkAccessFor(smackerId)
        val smacks = smackService.findAllInvolvedSmacksFor(smackerId)
        val locationIds: Flow<Long> = smacks.map { it.smackLocationId }
        val locations: Flow<SmackLocation> = locationService.findAllByIds(locationIds)
        val reviews: Flow<SmackLocationReview> = reviewService.findAllByLocationIdIn(locationIds)
        return SmackListDto(
            smacks = smacks.map { SmackDto.of(it) },
            smackLocations = locations.map { SmackLocationDto.of(it) },
            smackLocationReviews = reviews.map { SmackLocationReviewDto.of(it) }
        )
    }
}