package com.smackmap.smackmapbackend.smackspot

import com.smackmap.smackmapbackend.smackspot.review.SmackSpotReviewRequest
import com.smackmap.smackmapbackend.smackspot.review.SmackSpotReviewService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/smack/spot")
class SmackSpotController(
    private val smackSpotService: SmackSpotService,
    private val smackSpotReviewService: SmackSpotReviewService
) {
    @PostMapping
    suspend fun create(@RequestBody request: CreateSmackSpotRequest): ResponseEntity<SmackSpotDto> {
        val smackSpot = smackSpotService.create(request)
        return ResponseEntity.ok(SmackSpotDto.of(smackSpot))
    }

    @PostMapping("/search")
    suspend fun search(@RequestBody request: SmackSpotSearchRequest): ResponseEntity<Flow<SmackSpotDto>> {
        val locations = smackSpotService.search(request)
        return ResponseEntity.ok(locations.map { SmackSpotDto.of(it) })
    }

    @PostMapping("/review")
    suspend fun reviewLocation(@RequestBody request: SmackSpotReviewRequest): ResponseEntity<SmackSpotDto> {
        val smackSpot = smackSpotReviewService.addReview(request)
        return ResponseEntity.ok(SmackSpotDto.of(smackSpot))
    }
}