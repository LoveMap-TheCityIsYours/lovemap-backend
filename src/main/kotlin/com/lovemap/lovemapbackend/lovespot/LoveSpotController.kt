package com.lovemap.lovemapbackend.lovespot

import com.lovemap.lovemapbackend.lovespot.review.LoveSpotReviewDto
import com.lovemap.lovemapbackend.lovespot.review.LoveSpotReviewRequest
import com.lovemap.lovemapbackend.lovespot.review.LoveSpotReviewService
import com.lovemap.lovemapbackend.lovespot.risk.LoveSpotRisks
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/lovespots")
class LoveSpotController(
    private val loveSpotService: LoveSpotService,
    private val loveSpotReviewService: LoveSpotReviewService,
    private val loveSpotRisks: LoveSpotRisks
) {
    @PostMapping
    suspend fun create(@RequestBody request: CreateLoveSpotRequest): ResponseEntity<LoveSpotDto> {
        val loveSpot = loveSpotService.create(request)
        return ResponseEntity.ok(LoveSpotDto.of(loveSpot))
    }

    @GetMapping("/{id}")
    suspend fun find(@PathVariable id: Long): ResponseEntity<LoveSpotDto> {
        val loveSpot = loveSpotService.getById(id)
        return ResponseEntity.ok(LoveSpotDto.of(loveSpot))
    }

    @PostMapping("/search")
    suspend fun search(@RequestBody request: LoveSpotSearchRequest): ResponseEntity<Flow<LoveSpotDto>> {
        val locations = loveSpotService.search(request)
        return ResponseEntity.ok(locations.map { LoveSpotDto.of(it) })
    }

    @PutMapping("/reviews")
    suspend fun reviewSpot(@RequestBody request: LoveSpotReviewRequest): ResponseEntity<LoveSpotDto> {
        val loveSpot = loveSpotReviewService.addOrUpdateReview(request)
        return ResponseEntity.ok(LoveSpotDto.of(loveSpot))
    }

    @GetMapping("/reviews/bySpot/{loveSpotId}")
    suspend fun getReviewsForSpot(@PathVariable loveSpotId: Long): ResponseEntity<Flow<LoveSpotReviewDto>> {
        val loveSpots = loveSpotReviewService.findAllByLoveSpotIdIn(listOf(loveSpotId))
        return ResponseEntity.ok(loveSpots.map { LoveSpotReviewDto.of(it) })
    }

    @GetMapping("/reviews/byLover/{loverId}")
    suspend fun getReviewsByLover(@PathVariable loverId: Long): ResponseEntity<Flow<LoveSpotReviewDto>> {
        val loveSpots = loveSpotReviewService.findAllByReviewerId(loverId)
        return ResponseEntity.ok(loveSpots.map { LoveSpotReviewDto.of(it) })
    }

    @GetMapping("risks")
    suspend fun getRisks(): ResponseEntity<LoveSpotRisks> {
        return ResponseEntity.ok(loveSpotRisks)
    }
}