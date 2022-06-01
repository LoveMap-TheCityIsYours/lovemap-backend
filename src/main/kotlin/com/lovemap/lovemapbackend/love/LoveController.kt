package com.lovemap.lovemapbackend.love

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/loves")
class LoveController(
    private val loveService: LoveService,
    private val loveDeletionService: LoveDeletionService,
) {
    @GetMapping
    suspend fun listByLover(@RequestParam loverId: Long): ResponseEntity<Flow<LoveDto>> {
        val loveFlow = loveService.findAllInvolvedLovesFor(loverId)
        return ResponseEntity.ok(loveFlow.map { LoveDto.of(it) })
    }

    @PostMapping
    suspend fun create(@RequestBody request: CreateLoveRequest): ResponseEntity<LoveDto> {
        val love = loveService.create(request)
        return ResponseEntity.ok(LoveDto.of(love))
    }

    @PutMapping("/{id}")
    suspend fun update(
        @PathVariable id: Long,
        @RequestBody request: UpdateLoveRequest
    ): ResponseEntity<LoveDto> {
        val love = loveService.update(id, request)
        return ResponseEntity.ok(LoveDto.of(love))
    }

    @DeleteMapping("/{id}")
    suspend fun delete(@PathVariable id: Long): ResponseEntity<LoveDto> {
        val love = loveDeletionService.delete(id)
        return ResponseEntity.ok(LoveDto.of(love))
    }
}