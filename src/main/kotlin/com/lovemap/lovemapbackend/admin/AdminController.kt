package com.lovemap.lovemapbackend.admin

import com.lovemap.lovemapbackend.lovespot.LoveSpotDeletionService
import com.lovemap.lovemapbackend.lovespot.LoveSpotDto
import com.lovemap.lovemapbackend.lovespot.LoveSpotService
import com.lovemap.lovemapbackend.lovespot.UpdateLoveSpotRequest
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/admin")
class AdminController(
    private val loveSpotService: LoveSpotService,
    private val loveSpotDeletionService: LoveSpotDeletionService,
) {

    @PutMapping("/lovespots/{loveSpotId}")
    suspend fun updateLoveSpot(
        @PathVariable loveSpotId: Long,
        request: UpdateLoveSpotRequest
    ): ResponseEntity<LoveSpotDto> {
        val loveSpot = loveSpotService.update(loveSpotId, request)
        return ResponseEntity.ok(LoveSpotDto.of(loveSpot))
    }

    @DeleteMapping("/lovespots/{loveSpotId}")
    suspend fun deleteLoveSpot(@PathVariable loveSpotId: Long): ResponseEntity<LoveSpotDto> {
        val deletedSpot = loveSpotDeletionService.deleteSpot(loveSpotId)
        return ResponseEntity.ok(LoveSpotDto.of(deletedSpot))
    }
}