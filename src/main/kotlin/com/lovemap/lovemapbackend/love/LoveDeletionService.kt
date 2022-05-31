package com.lovemap.lovemapbackend.love

import com.lovemap.lovemapbackend.lovespot.review.LoveSpotReviewService
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class LoveDeletionService(
    private val loveService: LoveService,
    private val loveSpotReviewService: LoveSpotReviewService,
) {

    suspend fun delete(id: Long): Love {
        val love = loveService.getById(id)
        loveSpotReviewService.deleteReviewsByLove(love)
        loveService.delete(love)
        return love
    }
}