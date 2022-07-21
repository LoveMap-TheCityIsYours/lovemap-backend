package com.lovemap.lovemapbackend.admin

import com.lovemap.lovemapbackend.lovespot.LoveSpotDeletionService
import com.lovemap.lovemapbackend.lovespot.LoveSpotResponse
import com.lovemap.lovemapbackend.lovespot.LoveSpotService
import com.lovemap.lovemapbackend.lovespot.UpdateLoveSpotRequest
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

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
    ): ResponseEntity<LoveSpotResponse> {
        val loveSpot = loveSpotService.update(loveSpotId, request)
        return ResponseEntity.ok(LoveSpotResponse.of(loveSpot))
    }

    @DeleteMapping("/lovespots/{loveSpotId}")
    suspend fun deleteLoveSpot(@PathVariable loveSpotId: Long): ResponseEntity<LoveSpotResponse> {
        val deletedSpot = loveSpotDeletionService.deleteSpot(loveSpotId)
        return ResponseEntity.ok(LoveSpotResponse.of(deletedSpot))
    }
}