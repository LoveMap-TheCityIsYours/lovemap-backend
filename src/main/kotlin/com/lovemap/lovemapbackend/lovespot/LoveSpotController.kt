package com.lovemap.lovemapbackend.lovespot

import com.lovemap.lovemapbackend.lovespot.risk.LoveSpotRisks
import com.lovemap.lovemapbackend.lovespot.list.LoveSpotListService
import com.lovemap.lovemapbackend.utils.ValidatorService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/lovespots")
class LoveSpotController(
    private val loveSpotService: LoveSpotService,
    private val loveSpotListService: LoveSpotListService,
    private val loveSpotRisks: LoveSpotRisks,
    private val validatorService: ValidatorService,
) {
    @PostMapping
    suspend fun create(@RequestBody request: CreateLoveSpotRequest): ResponseEntity<LoveSpotDto> {
        validatorService.validate(request)
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
        val loveSpots = loveSpotListService.list(request)
        return ResponseEntity.ok(loveSpots.map { LoveSpotDto.of(it) })
    }

    @PostMapping("/list")
    suspend fun list(@RequestBody request: LoveSpotListRequest): ResponseEntity<Flow<LoveSpotDto>> {
        val loveSpots = loveSpotListService.list(request)
        return ResponseEntity.ok(loveSpots.map { LoveSpotDto.of(it) })
    }

    @PostMapping("/advancedList")
    suspend fun advancedSearch(
        @RequestParam(required = true) listOrdering: ListOrdering,
        @RequestParam(required = true) listLocation: ListLocation,
        @RequestBody request: LoveSpotAdvancedListRequest
    ): ResponseEntity<Flow<LoveSpotDto>> {
        val loveSpots = loveSpotListService.advancedList(listOrdering, listLocation, request)
        return ResponseEntity.ok(loveSpots.map { LoveSpotDto.of(it) })
    }

    @GetMapping("risks")
    suspend fun getRisks(): ResponseEntity<LoveSpotRisks> {
        return ResponseEntity.ok(loveSpotRisks)
    }
}