package com.lovemap.lovemapbackend.love

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/loves")
class LoveController(
    private val loveService: LoveService,
    private val loveDeletionService: LoveDeletionService,
) {
    @GetMapping
    suspend fun listByLover(@RequestParam loverId: Long): ResponseEntity<List<LoveDto>> {
        val loveDtoList = loveService.findAllInvolvedLovesFor(loverId)
        return ResponseEntity.ok(loveDtoList)
    }

    @PostMapping
    suspend fun create(@RequestBody request: CreateLoveRequest): ResponseEntity<LoveDto> {
        val loveDto = loveService.create(request)
        return ResponseEntity.ok(loveDto)
    }

    @PutMapping("/{id}")
    suspend fun update(
        @PathVariable id: Long,
        @RequestBody request: UpdateLoveRequest
    ): ResponseEntity<LoveDto> {
        val loveDto = loveService.update(id, request)
        return ResponseEntity.ok(loveDto)
    }

    @DeleteMapping("/{id}")
    suspend fun delete(@PathVariable id: Long): ResponseEntity<LoveDto> {
        val loveDto = loveDeletionService.delete(id)
        return ResponseEntity.ok(loveDto)
    }
}