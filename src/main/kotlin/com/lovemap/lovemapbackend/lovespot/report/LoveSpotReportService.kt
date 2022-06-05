package com.lovemap.lovemapbackend.lovespot.report

import com.lovemap.lovemapbackend.lover.LoverPointService
import com.lovemap.lovemapbackend.lovespot.LoveSpot
import com.lovemap.lovemapbackend.lovespot.LoveSpotService
import com.lovemap.lovemapbackend.security.AuthorizationService
import kotlinx.coroutines.flow.Flow
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class LoveSpotReportService(
    private val authorizationService: AuthorizationService,
    private val loveSpotService: LoveSpotService,
    private val loverPointService: LoverPointService,
    private val repository: LoveSpotReportRepository
) {

    fun findAllByLoveSpotIdIn(loveSpotIds: List<Long>): Flow<LoveSpotReport> {
        return repository.findAllByLoveSpotIdIn(loveSpotIds)
    }

    fun findAllByReporterId(loverId: Long): Flow<LoveSpotReport> {
        return repository.findAllByLoverId(loverId)
    }

    suspend fun addOrUpdateReport(request: LoveSpotReportRequest): LoveSpot {
        authorizationService.checkAccessFor(request.loverId)
        val spotReport = repository
            .findByLoverIdAndLoveSpotId(request.loverId, request.loveSpotId)
        if (spotReport != null) {
            return updateReport(spotReport, request)
        }
        return addReport(request)
    }

    private suspend fun updateReport(
        spotReport: LoveSpotReport,
        request: LoveSpotReportRequest
    ): LoveSpot {
        spotReport.reportText = request.reportText.trim()
        repository.save(spotReport)
        return loveSpotService.getById(spotReport.loveSpotId)
    }

    private suspend fun addReport(request: LoveSpotReportRequest): LoveSpot {
        val review = repository.save(
            LoveSpotReport(
                loverId = request.loverId,
                loveSpotId = request.loveSpotId,
                reportText = request.reportText.trim(),
            )
        )
        val loveSpot = loveSpotService.updateNumberOfReports(request.loveSpotId, request)
        loverPointService.addPointsForReport(review, loveSpot)
        return loveSpot
    }

    suspend fun deleteReportsOfSpot(loveSpotId: Long) {
        repository.deleteByLoveSpotId(loveSpotId)
    }
}