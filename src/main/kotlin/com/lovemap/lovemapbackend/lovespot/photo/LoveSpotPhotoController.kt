package com.lovemap.lovemapbackend.lovespot.photo

import com.lovemap.lovemapbackend.lovespot.photo.like.PhotoLikeService
import kotlinx.coroutines.reactive.asFlow
import org.springframework.http.codec.multipart.FilePart
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Flux

@RestController
@RequestMapping("/lovespots")
class LoveSpotPhotoController(
    private val loveSpotPhotoService: LoveSpotPhotoService,
    private val photoDeletionService: PhotoDeletionService,
    private val photoLikeService: PhotoLikeService
) {
    @PostMapping("{loveSpotId}/photos")
    suspend fun uploadToSpot(
        @PathVariable loveSpotId: Long,
        @RequestPart("photos") filePartFlux: Flux<FilePart>
    ) {
        loveSpotPhotoService.uploadToSpot(loveSpotId, filePartFlux.asFlow())
    }

    @PostMapping("{loveSpotId}/reviews/{reviewId}/photos")
    suspend fun uploadToSpotReview(
        @PathVariable loveSpotId: Long,
        @PathVariable reviewId: Long,
        @RequestPart("photos") filePartFlux: Flux<FilePart>
    ) {
        loveSpotPhotoService.uploadToReview(loveSpotId, reviewId, filePartFlux.asFlow())
    }

    @GetMapping("{loveSpotId}/photos")
    suspend fun getPhotosForSpot(@PathVariable loveSpotId: Long): List<LoveSpotPhotoResponse> {
        return loveSpotPhotoService.getPhotosForSpot(loveSpotId)
    }

    @GetMapping("{loveSpotId}/reviews/{reviewId}/photos")
    suspend fun getPhotosForReview(
        @PathVariable loveSpotId: Long,
        @PathVariable reviewId: Long,
    ): List<LoveSpotPhotoResponse> {
        return loveSpotPhotoService.getPhotosForReview(loveSpotId, reviewId)
    }

    @DeleteMapping("{loveSpotId}/photos/{photoId}")
    suspend fun deletePhoto(
        @PathVariable loveSpotId: Long,
        @PathVariable photoId: Long
    ): List<LoveSpotPhotoResponse> {
        return photoDeletionService.deletePhoto(loveSpotId, photoId)
    }

    @PostMapping("{loveSpotId}/photos/{photoId}/like")
    suspend fun likePhoto(
        @PathVariable loveSpotId: Long,
        @PathVariable photoId: Long
    ): LoveSpotPhotoResponse {
        return photoLikeService.likePhoto(loveSpotId, photoId)
    }

    @PostMapping("{loveSpotId}/photos/{photoId}/dislike")
    suspend fun dislikePhoto(
        @PathVariable loveSpotId: Long,
        @PathVariable photoId: Long
    ): LoveSpotPhotoResponse {
        return photoLikeService.dislikePhoto(loveSpotId, photoId)
    }
}











