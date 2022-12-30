package com.lovemap.lovemapbackend.lovespot.photo

import java.time.Instant

data class LoveSpotPhotoResponse(
    val id: Long,
    val loveSpotId: Long,
    val reviewId: Long?,
    val uploadedBy: Long,
    val uploadedAt: Instant,
    val likes: Int,
    val dislikes: Int,
    val url: String
) {
    companion object {
        fun of(photo: LoveSpotPhoto): LoveSpotPhotoResponse {
            return LoveSpotPhotoResponse(
                id = photo.id,
                loveSpotId = photo.loveSpotId,
                reviewId = photo.loveSpotReviewId,
                uploadedBy = photo.uploadedBy,
                uploadedAt = photo.uploadedAt.toInstant(),
                likes = photo.likes,
                dislikes = photo.dislikes,
                url = photo.url
            )
        }
    }
}