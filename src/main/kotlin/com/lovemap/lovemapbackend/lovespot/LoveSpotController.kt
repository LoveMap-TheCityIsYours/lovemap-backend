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
    private val loveSpotRisks: LoveSpotRisks
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

    @PostMapping("/search")
    suspend fun search(@RequestBody request: LoveSpotSearchRequest): ResponseEntity<Flow<LoveSpotDto>> {
        val locations = loveSpotService.search(request)
        return ResponseEntity.ok(locations.map { LoveSpotDto.of(it) })
    }

    @GetMapping("risks")
    suspend fun getRisks(): ResponseEntity<LoveSpotRisks> {
        return ResponseEntity.ok(loveSpotRisks)
    }
}