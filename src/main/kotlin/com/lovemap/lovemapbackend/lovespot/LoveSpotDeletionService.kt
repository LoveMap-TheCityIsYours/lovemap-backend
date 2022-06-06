package com.lovemap.lovemapbackend.lovespot

import com.lovemap.lovemapbackend.love.LoveService
import com.lovemap.lovemapbackend.lovespot.report.LoveSpotReportService
import com.lovemap.lovemapbackend.lovespot.review.LoveSpotReviewService
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class LoveSpotDeletionService(
    private val loveService: LoveService,
    private val loveSpotService: LoveSpotService,
    private val loveSpotReviewService: LoveSpotReviewService,
    private val loveSpotReportService: LoveSpotReportService,
) {

    suspend fun deleteSpot(spotId: Long): LoveSpot {
        val loveSpot = loveSpotService.getById(spotId)
        loveSpotReportService.deleteReportsOfSpot(spotId)
        loveSpotReviewService.deleteReviewsOfSpot(spotId)
        loveService.deleteLovesBySpot(loveSpot)
        loveSpotService.deleteLoveSpot(loveSpot)
        return loveSpot
    }
}