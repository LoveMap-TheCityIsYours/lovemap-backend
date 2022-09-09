package com.lovemap.lovemapbackend.lovespot

import com.lovemap.lovemapbackend.lovespot.query.*
import com.lovemap.lovemapbackend.lovespot.risk.LoveSpotRisks
import com.lovemap.lovemapbackend.utils.ValidatorService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/lovespots")
class LoveSpotController(
    private val loveSpotService: LoveSpotService,
    private val loveSpotListService: LoveSpotQueryService,
    private val recommendationService: LoveSpotRecommendationService,
    private val loveSpotRisks: LoveSpotRisks,
    private val validatorService: ValidatorService,
) {
    @PostMapping
    suspend fun create(@RequestBody request: CreateLoveSpotRequest): ResponseEntity<LoveSpotResponse> {
        validatorService.validate(request)
        val loveSpot = loveSpotService.create(request)
        return ResponseEntity.ok(LoveSpotResponse.of(loveSpot))
    }

    @PutMapping("/{id}")
    suspend fun update(
        @PathVariable id: Long,
        @RequestBody request: UpdateLoveSpotRequest
    ): ResponseEntity<LoveSpotResponse> {
        val loveSpot = loveSpotService.update(id, request)
        return ResponseEntity.ok(LoveSpotResponse.of(loveSpot))
    }

    @GetMapping("/{id}")
    suspend fun find(@PathVariable id: Long): ResponseEntity<LoveSpotResponse> {
        val loveSpot = loveSpotService.getById(id)
        return ResponseEntity.ok(LoveSpotResponse.of(loveSpot))
    }

    @PostMapping("/list")
    suspend fun list(@RequestBody request: LoveSpotListRequest): ResponseEntity<Flow<LoveSpotResponse>> {
        val loveSpots = loveSpotListService.list(request)
        return ResponseEntity.ok(loveSpots.map { LoveSpotResponse.of(it) })
    }

    @Deprecated("will be removed soon")
    @PostMapping("/advancedList")
    suspend fun advancedList(
        @RequestParam listOrdering: ListOrderingRequest,
        @RequestParam listLocation: ListLocationRequest,
        @RequestBody request: LoveSpotSearchRequest
    ): ResponseEntity<List<LoveSpotResponse>> {
        val loveSpots = loveSpotListService.search(listOrdering, listLocation, request)
        return ResponseEntity.ok(loveSpots.map { LoveSpotResponse.of(it) })
    }

    @PostMapping("/search")
    suspend fun search(
        @RequestParam listOrdering: ListOrderingRequest,
        @RequestParam listLocation: ListLocationRequest,
        @RequestBody request: LoveSpotSearchRequest
    ): ResponseEntity<List<LoveSpotResponse>> {
        val loveSpots = loveSpotListService.search(listOrdering, listLocation, request)
        return ResponseEntity.ok(loveSpots.map { LoveSpotResponse.of(it) })
    }

    @PostMapping("/recommendations")
    suspend fun recommendations(@RequestBody request: RecommendationsRequest): ResponseEntity<RecommendationsResponse> {
        val recommendations = recommendationService.getRecommendations(request)
        return ResponseEntity.ok(recommendations)
    }

    @GetMapping("risks")
    suspend fun getRisks(): ResponseEntity<LoveSpotRisks> {
        return ResponseEntity.ok(loveSpotRisks)
    }
}