package com.lovemap.lovemapbackend.lover

import com.lovemap.lovemapbackend.lover.ranks.LoverRanks
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/lover")
class LoverController(
    private val loverRelationService: LoverRelationService,
    private val loverService: LoverService,
    private val loverContributionsService: LoverContributionsService,
    private val loverRanks: LoverRanks
) {
    @GetMapping("/contributions/{loverId}")
    suspend fun contributions(@PathVariable loverId: Long): ResponseEntity<LoverContributionsDto> {
        val loveListDto = loverContributionsService.list(loverId)
        return ResponseEntity.ok(loveListDto)
    }

    @GetMapping("/{loverId}")
    suspend fun getLover(@PathVariable loverId: Long): ResponseEntity<LoverRelationsDto> {
        return ResponseEntity.ok(loverRelationService.getWithRelations(loverId))
    }

    @GetMapping
    suspend fun getLoverByUuid(@RequestParam uuid: String)
            : ResponseEntity<LoverViewDto> {
        val loverViewDto = loverRelationService.getByUuid(uuid)
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