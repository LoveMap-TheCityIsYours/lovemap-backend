package com.lovemap.lovemapbackend.lovespot.photo

data class LoveSpotPhotoResponse(
    val id: Long,
    val loveSpotId: Long,
    val reviewId: Long?,
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
                likes = photo.likes,
                dislikes = photo.dislikes,
                url = photo.url
            )
        }
    }
}