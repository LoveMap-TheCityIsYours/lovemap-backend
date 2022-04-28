package com.smackmap.smackmapbackend.smacker

import com.smackmap.smackmapbackend.smacker.ranks.SmackerRanks
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/smacker")
class SmackerController(
    private val smackerRelationService: SmackerRelationService,
    private val smackerService: SmackerService,
    private val smackerRanks: SmackerRanks
) {
    @GetMapping("/{smackerId}")
    suspend fun getSmacker(@PathVariable smackerId: Long): ResponseEntity<SmackerRelationsDto> {
        return ResponseEntity.ok(smackerRelationService.getWithRelations(smackerId))
    }

    @GetMapping("/byLink/")
    suspend fun getSmackerByLink(@RequestParam smackerLink: String)
            : ResponseEntity<SmackerViewDto> {
        val smackerViewDto = smackerRelationService.getByLink(smackerLink)
        return ResponseEntity.ok(smackerViewDto)
    }

    @PostMapping("/{smackerId}/shareableLink")
    suspend fun generateSmackerLink(@PathVariable smackerId: Long)
            : ResponseEntity<SmackerDto> {
        val smacker = smackerService.generateSmackerLink(smackerId)
        return ResponseEntity.ok(SmackerDto.of(smacker))
    }

    @DeleteMapping("/{smackerId}/shareableLink")
    suspend fun deleteSmackerLink(@PathVariable smackerId: Long)
            : ResponseEntity<SmackerDto> {
        val smacker = smackerService.deleteSmackerLink(smackerId)
        return ResponseEntity.ok(SmackerDto.of(smacker))
    }

    @GetMapping("ranks")
    suspend fun getRanks(): ResponseEntity<SmackerRanks> {
        return ResponseEntity.ok(smackerRanks)
    }
}