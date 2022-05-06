package com.smackmap.smackmapbackend.smackspot

import com.smackmap.smackmapbackend.smackspot.review.SmackSpotReview
import com.smackmap.smackmapbackend.smackspot.review.SmackSpotReviewDto
import com.smackmap.smackmapbackend.smackspot.review.SmackSpotReviewRequest
import com.smackmap.smackmapbackend.smackspot.review.SmackSpotReviewService
import com.smackmap.smackmapbackend.smackspot.risk.SmackSpotRisks
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/smackspot")
class SmackSpotController(
    private val smackSpotService: SmackSpotService,
    private val smackSpotReviewService: SmackSpotReviewService,
    private val smackSpotRisks: SmackSpotRisks
) {
    @PostMapping
    suspend fun create(@RequestBody request: CreateSmackSpotRequest): ResponseEntity<SmackSpotDto> {
        val smackSpot = smackSpotService.create(request)
        return ResponseEntity.ok(SmackSpotDto.of(smackSpot))
    }

    @GetMapping("/{id}")
    suspend fun find(@PathVariable id: Long): ResponseEntity<SmackSpotDto> {
        val smackSpot = smackSpotService.getById(id)
        return ResponseEntity.ok(SmackSpotDto.of(smackSpot))
    }

    @PostMapping("/search")
    suspend fun search(@RequestBody request: SmackSpotSearchRequest): ResponseEntity<Flow<SmackSpotDto>> {
        val locations = smackSpotService.search(request)
        return ResponseEntity.ok(locations.map { SmackSpotDto.of(it) })
    }

    @PostMapping("/review")
    suspend fun reviewSpot(@RequestBody request: SmackSpotReviewRequest): ResponseEntity<SmackSpotDto> {
        val smackSpot = smackSpotReviewService.addReview(request)
        return ResponseEntity.ok(SmackSpotDto.of(smackSpot))
    }

    @GetMapping("/review/{smackSpotId}")
    suspend fun getReviews(@PathVariable smackSpotId: Long): ResponseEntity<List<SmackSpotReviewDto>> {
        val smackSpots = smackSpotReviewService.findAllBySmackSpotIdIn(listOf(smackSpotId))
        return ResponseEntity.ok(smackSpots.map { SmackSpotReviewDto.of(it) }.toList())
    }

    @GetMapping("risks")
    suspend fun getRisks(): ResponseEntity<SmackSpotRisks> {
        return ResponseEntity.ok(smackSpotRisks)
    }
}