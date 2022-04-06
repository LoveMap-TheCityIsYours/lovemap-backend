package com.smackmap.smackmapbackend.smacker

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/smacker")
class SmackerController(
    private val smackerService: SmackerService
) {
    @PostMapping
    fun register(createSmackerRequest: CreateSmackerRequest): ResponseEntity<SmackerResponse> {
        val smackerResponse = smackerService.createSmacker(createSmackerRequest)
        return ResponseEntity.ok(smackerResponse)
    }
}