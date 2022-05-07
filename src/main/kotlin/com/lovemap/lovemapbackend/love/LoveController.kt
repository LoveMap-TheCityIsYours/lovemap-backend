package com.lovemap.lovemapbackend.love

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/love")
class LoveController(
    private val loveService: LoveService,
    private val loveListService: LoveListService
) {
    @GetMapping("/{loverId}")
    suspend fun list(@PathVariable loverId: Long): ResponseEntity<LoveListDto> {
        val loveListDto = loveListService.list(loverId)
        return ResponseEntity.ok(loveListDto)
    }

    @PostMapping
    suspend fun create(@RequestBody request: CreateLoveRequest): ResponseEntity<LoveDto> {
        val love = loveService.create(request)
        return ResponseEntity.ok(LoveDto.of(love))
    }
}