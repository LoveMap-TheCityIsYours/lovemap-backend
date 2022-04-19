package com.smackmap.smackmapbackend.smack

import com.smackmap.smackmapbackend.smack.location.SmackService
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping

@Controller
@RequestMapping("/smack")
class SmackController(
    private val smackService: SmackService
) {
    @GetMapping("/{smackerId}")
    suspend fun list(@PathVariable smackerId: Long): ResponseEntity<SmackListDto> {
        val smackListDto = smackService.list(smackerId)
        return ResponseEntity.ok(smackListDto)
    }

    @PostMapping
    suspend fun create(request: CreateSmackRequest): ResponseEntity<SmackDto> {
        val smack = smackService.create(request)
        return ResponseEntity.ok(SmackDto.of(smack))
    }
}