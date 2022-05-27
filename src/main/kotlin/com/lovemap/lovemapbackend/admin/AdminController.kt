package com.lovemap.lovemapbackend.admin

import com.lovemap.lovemapbackend.lovespot.LoveSpotDeletionService
import com.lovemap.lovemapbackend.lovespot.LoveSpotDto
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/admin")
class AdminController(
    private val loveSpotDeletionService: LoveSpotDeletionService
) {

    @DeleteMapping("/lovespots/{loveSpotId}")
    suspend fun deleteLoveSpot(@PathVariable loveSpotId: Long): ResponseEntity<LoveSpotDto> {
        val deletedSpot = loveSpotDeletionService.deleteSpot(loveSpotId)
        return ResponseEntity.ok(LoveSpotDto.of(deletedSpot))
    }
}