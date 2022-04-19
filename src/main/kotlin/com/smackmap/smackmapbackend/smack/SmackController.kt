package com.smackmap.smackmapbackend.smack

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/smack")
class SmackController(
    private val smackService: SmackService,
    private val smackListService: SmackListService
) {
    @GetMapping("/{smackerId}")
    suspend fun list(@PathVariable smackerId: Long): ResponseEntity<SmackListDto> {
        val smackListDto = smackListService.list(smackerId)
        return ResponseEntity.ok(smackListDto)
    }

    @PostMapping
    suspend fun create(@RequestBody request: CreateSmackRequest): ResponseEntity<SmackDto> {
        val smack = smackService.create(request)
        return ResponseEntity.ok(SmackDto.of(smack))
    }
}