package com.smackmap.smackmapbackend.smacker

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/smacker")
class SmackerController(
    private val smackerService: SmackerService,
) {

    @GetMapping
    suspend fun test() = ResponseEntity.ok("banan")

    @GetMapping("/{smackerId}")
    suspend fun getSmacker(@PathVariable smackerId: Long): ResponseEntity<SmackerResponse> {
        return ResponseEntity.ok(SmackerResponse.of(smackerService.getById(smackerId)))
    }

    @GetMapping("/byLink")
    suspend fun getSmackerByLink(@RequestBody request: GetSmackerByLinkRequest)
            : ResponseEntity<SmackerResponse> {
        val link = request.smackerLink
        val smacker = smackerService.getByLink(link)
        return ResponseEntity.ok(SmackerResponse.of(smacker))
    }

    @PutMapping("/generateLink")
    suspend fun generateSmackerLink(@RequestBody request: GenerateSmackerLinkRequest)
            : ResponseEntity<SmackerLinkResponse> {
        val link = smackerService.generateSmackerLink(request.smackerId)
        return ResponseEntity.ok(SmackerLinkResponse(request.smackerId, link))
    }
}