package com.lovemap.lovemapbackend.lovespot.review

import com.lovemap.lovemapbackend.lovespot.LoveSpotDto
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/lovespots/reviews")
class LoveSpotReviewController(
    private val loveSpotReviewService: LoveSpotReviewService,
) {
    @PutMapping
    suspend fun reviewSpot(@RequestBody request: LoveSpotReviewRequest): ResponseEntity<LoveSpotDto> {
        val loveSpot = loveSpotReviewService.addOrUpdateReview(request)
        return ResponseEntity.ok(LoveSpotDto.of(loveSpot))
    }

    @GetMapping("/bySpot/{loveSpotId}")
    suspend fun getReviewsForSpot(@PathVariable loveSpotId: Long): ResponseEntity<Flow<LoveSpotReviewDto>> {
        val loveSpots = loveSpotReviewService.findAllByLoveSpotIdIn(listOf(loveSpotId))
        return ResponseEntity.ok(loveSpots.map { LoveSpotReviewDto.of(it) })
    }

    @GetMapping("/byLover/{loverId}")
    suspend fun getReviewsByLover(@PathVariable loverId: Long): ResponseEntity<Flow<LoveSpotReviewDto>> {
        val loveSpots = loveSpotReviewService.findAllByReviewerId(loverId)
        return ResponseEntity.ok(loveSpots.map { LoveSpotReviewDto.of(it) })
    }
}