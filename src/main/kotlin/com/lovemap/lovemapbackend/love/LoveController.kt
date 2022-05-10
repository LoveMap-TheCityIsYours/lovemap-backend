package com.lovemap.lovemapbackend.love

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/love")
class LoveController(
    private val loveService: LoveService
) {
    @GetMapping("/{loverId}")
    suspend fun list(@PathVariable loverId: Long): ResponseEntity<Flow<LoveDto>> {
        val loveFlow = loveService.findAllInvolvedLovesFor(loverId)
        return ResponseEntity.ok(loveFlow.map { LoveDto.of(it) })
    }

    @PostMapping
    suspend fun create(@RequestBody request: CreateLoveRequest): ResponseEntity<LoveDto> {
        val love = loveService.create(request)
        return ResponseEntity.ok(LoveDto.of(love))
    }
}