package com.smackmap.smackmapbackend.smacker

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/smacker")
class SmackerController(
    private val smackerRelationService: SmackerRelationService,
    private val smackerService: SmackerService,
) {

    @GetMapping
    suspend fun test() = ResponseEntity.ok("banan")

    @GetMapping("/{smackerId}")
    suspend fun getSmacker(@PathVariable smackerId: Long): ResponseEntity<SmackerRelationsDto> {
        return ResponseEntity.ok(smackerRelationService.getDtoById(smackerId))
    }

    @GetMapping("/byLink/")
    suspend fun getSmackerByLink(@RequestParam smackerLink: String)
            : ResponseEntity<SmackerViewDto> {
        val smackerViewDto = smackerRelationService.getByLink(smackerLink)
        return ResponseEntity.ok(smackerViewDto)
    }

    @PutMapping("/generateLink")
    suspend fun generateSmackerLink(@RequestBody request: GenerateSmackerLinkRequest)
            : ResponseEntity<SmackerLinkDto> {
        val link = smackerService.generateSmackerLink(request.smackerId)
        return ResponseEntity.ok(SmackerLinkDto(request.smackerId, link))
    }
}