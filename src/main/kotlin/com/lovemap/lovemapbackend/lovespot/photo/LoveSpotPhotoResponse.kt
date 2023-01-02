package com.lovemap.lovemapbackend.lovespot.photo

import java.time.Instant

data class LoveSpotPhotoResponse(
    val id: Long,
    val loveSpotId: Long,
    val reviewId: Long?,
    val uploadedBy: Long,
    val uploadedAt: Instant,
    val likes: Int,
    val likers: Set<Long>,
    val dislikes: Int,
    val dislikers: Set<Long>,
    val url: String
) {
    companion object {
        fun of(photo: LoveSpotPhoto, likers: Set<Long>, dislikers: Set<Long>): LoveSpotPhotoResponse {
            return LoveSpotPhotoResponse(
                id = photo.id,
                loveSpotId = photo.loveSpotId,
                reviewId = photo.loveSpotReviewId,
                uploadedBy = photo.uploadedBy,
                uploadedAt = photo.uploadedAt.toInstant(),
                likes = photo.likes,
                likers = likers,
                dislikes = photo.dislikes,
                dislikers = dislikers,
                url = photo.url
            )
        }
    }
}
