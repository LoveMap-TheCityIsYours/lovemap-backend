package com.lovemap.lovemapbackend.lovespot.report

import com.lovemap.lovemapbackend.lovespot.LoveSpotResponse
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/lovespots/reports")
class LoveSpotReportController(
    private val loveSpotReportService: LoveSpotReportService
) {

    @PutMapping
    suspend fun reportSpot(@RequestBody request: LoveSpotReportRequest): ResponseEntity<LoveSpotResponse> {
        val loveSpot = loveSpotReportService.addOrUpdateReport(request)
        return ResponseEntity.ok(LoveSpotResponse.of(loveSpot))
    }

    @GetMapping("/bySpot/{loveSpotId}")
    suspend fun getReportsForSpot(@PathVariable loveSpotId: Long): ResponseEntity<Flow<LoveSpotReportDto>> {
        val loveSpotReports = loveSpotReportService.findAllByLoveSpotIdIn(listOf(loveSpotId))
        return ResponseEntity.ok(loveSpotReports.map { LoveSpotReportDto.of(it) })
    }

    @GetMapping("/byLover/{loverId}")
    suspend fun getReportsByLover(@PathVariable loverId: Long): ResponseEntity<Flow<LoveSpotReportDto>> {
        val loveSpots = loveSpotReportService.findAllByReporterId(loverId)
        return ResponseEntity.ok(loveSpots.map { LoveSpotReportDto.of(it) })
    }
}