package com.lovemap.lovemapbackend.lovespot.photo

import kotlinx.coroutines.reactive.asFlow
import org.springframework.http.codec.multipart.FilePart
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Flux

@RestController
@RequestMapping("/lovespots")
class LoveSpotPhotoController(
    private val loveSpotPhotoService: LoveSpotPhotoService
) {
    @PostMapping("{loveSpotId}/photos")
    suspend fun uploadToSpot(
        @PathVariable loveSpotId: Long,
        @RequestPart("files") filePartFlux: Flux<FilePart>
    ) {
        loveSpotPhotoService.uploadToSpot(loveSpotId, filePartFlux.asFlow())
    }

    @PostMapping("{loveSpotId}/reviews/{reviewId}/photos")
    suspend fun uploadToSpotReview(
        @PathVariable loveSpotId: Long,
        @PathVariable reviewId: Long,
        @RequestPart("files") filePartFlux: Flux<FilePart>
    ) {
        loveSpotPhotoService.uploadToReview(loveSpotId, reviewId, filePartFlux.asFlow())
    }
}
