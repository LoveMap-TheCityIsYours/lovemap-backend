package com.lovemap.lovemapbackend.lovespot

import com.lovemap.lovemapbackend.lovespot.risk.LoveSpotRisks
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/lovespots")
class LoveSpotController(
    private val loveSpotService: LoveSpotService,
    private val loveSpotSearchService: LoveSpotSearchService,
    private val loveSpotRisks: LoveSpotRisks,
) {
    @PostMapping
    suspend fun create(@RequestBody request: CreateLoveSpotRequest): ResponseEntity<LoveSpotDto> {
        val loveSpot = loveSpotService.create(request)
        return ResponseEntity.ok(LoveSpotDto.of(loveSpot))
    }

    @PutMapping("/{id}")
    suspend fun update(
        @PathVariable id: Long,
        @RequestBody request: UpdateLoveSpotRequest
    ): ResponseEntity<LoveSpotDto> {
        val loveSpot = loveSpotService.update(id, request)
        return ResponseEntity.ok(LoveSpotDto.of(loveSpot))
    }

    @GetMapping("/{id}")
    suspend fun find(@PathVariable id: Long): ResponseEntity<LoveSpotDto> {
        val loveSpot = loveSpotService.getById(id)
        return ResponseEntity.ok(LoveSpotDto.of(loveSpot))
    }

    @Deprecated("will be removed later")
    @PostMapping("/search")
    suspend fun search(@RequestBody request: LoveSpotListRequest): ResponseEntity<Flow<LoveSpotDto>> {
        val loveSpots = loveSpotService.list(request)
        return ResponseEntity.ok(loveSpots.map { LoveSpotDto.of(it) })
    }

    @PostMapping("/list")
    suspend fun list(@RequestBody request: LoveSpotListRequest): ResponseEntity<Flow<LoveSpotDto>> {
        val loveSpots = loveSpotService.list(request)
        return ResponseEntity.ok(loveSpots.map { LoveSpotDto.of(it) })
    }

    @PostMapping("/advancedSearch")
    suspend fun advancedSearch(
        @RequestParam(required = true) searchResultOrdering: SearchResultOrdering,
        @RequestParam(required = true) searchLocation: SearchLocation,
        @RequestBody request: LoveSpotSearchRequest
    ): ResponseEntity<List<LoveSpotDto>> {
        val loveSpots = loveSpotSearchService.search(searchResultOrdering, searchLocation, request)
        return ResponseEntity.ok(loveSpots)
    }

    @GetMapping("risks")
    suspend fun getRisks(): ResponseEntity<LoveSpotRisks> {
        return ResponseEntity.ok(loveSpotRisks)
    }
}