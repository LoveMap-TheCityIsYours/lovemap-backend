package com.smackmap.smackmapbackend.smack.location

import com.smackmap.smackmapbackend.smack.location.review.SmackLocationReviewRequest
import com.smackmap.smackmapbackend.smack.location.review.SmackLocationReviewService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.*

@Controller
@RequestMapping("/smack/location")
class SmackLocationController(
    private val smackLocationService: SmackLocationService,
    private val smackLocationReviewService: SmackLocationReviewService
) {
    @PostMapping
    suspend fun create(@RequestBody request: CreateSmackLocationRequest): ResponseEntity<SmackLocationDto> {
        val smackLocation = smackLocationService.create(request)
        return ResponseEntity.ok(SmackLocationDto.of(smackLocation))
    }

    @PostMapping("/search")
    suspend fun search(@RequestBody request: SmackLocationSearchRequest): ResponseEntity<Flow<SmackLocationDto>> {
        val locations = smackLocationService.search(request)
        return ResponseEntity.ok(locations.map { SmackLocationDto.of(it) })
    }

    @PostMapping("/review")
    suspend fun reviewLocation(request: SmackLocationReviewRequest): ResponseEntity<SmackLocationDto> {
        val smackLocation = smackLocationReviewService.addReview(request)
        return ResponseEntity.ok(SmackLocationDto.of(smackLocation))
    }
}