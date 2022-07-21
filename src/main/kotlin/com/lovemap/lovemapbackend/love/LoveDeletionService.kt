package com.lovemap.lovemapbackend.love

import com.lovemap.lovemapbackend.lovespot.review.LoveSpotReviewService
import com.lovemap.lovemapbackend.security.AuthorizationService
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class LoveDeletionService(
    private val loveService: LoveService,
    private val authorizationService: AuthorizationService,
    private val loveConverter: LoveConverter,
    private val loveSpotReviewService: LoveSpotReviewService,
) {

    suspend fun delete(id: Long): LoveResponse {
        val love = loveService.getById(id)
        val caller = authorizationService.checkAccessFor(love)
        loveSpotReviewService.deleteReviewsByLove(love)
        loveService.delete(love)
        return loveConverter.toDto(caller, love)
    }
}