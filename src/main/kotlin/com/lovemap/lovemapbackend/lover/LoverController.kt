package com.lovemap.lovemapbackend.lover

import com.lovemap.lovemapbackend.lover.ranks.LoverRanks
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/lover")
class LoverController(
    private val loverRelationService: LoverRelationService,
    private val loverService: LoverService,
    private val loverRanks: LoverRanks
) {
    @GetMapping("/{loverId}")
    suspend fun getLover(@PathVariable loverId: Long): ResponseEntity<LoverRelationsDto> {
        return ResponseEntity.ok(loverRelationService.getWithRelations(loverId))
    }

    @GetMapping("/byLink/")
    suspend fun getLoverByLink(@RequestParam loverLink: String)
            : ResponseEntity<LoverViewDto> {
        val loverViewDto = loverRelationService.getByLink(loverLink)
        return ResponseEntity.ok(loverViewDto)
    }

    @PostMapping("/{loverId}/shareableLink")
    suspend fun generateLoverLink(@PathVariable loverId: Long)
            : ResponseEntity<LoverDto> {
        val lover = loverService.generateLoverLink(loverId)
        return ResponseEntity.ok(LoverDto.of(lover))
    }

    @DeleteMapping("/{loverId}/shareableLink")
    suspend fun deleteLoverLink(@PathVariable loverId: Long)
            : ResponseEntity<LoverDto> {
        val lover = loverService.deleteLoverLink(loverId)
        return ResponseEntity.ok(LoverDto.of(lover))
    }

    @GetMapping("ranks")
    suspend fun getRanks(): ResponseEntity<LoverRanks> {
        return ResponseEntity.ok(loverRanks)
    }
}