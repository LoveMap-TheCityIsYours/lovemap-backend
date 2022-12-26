package com.lovemap.lovemapbackend.lovespot.photo

data class LoveSpotPhotoResponse(
    val loveSpotId: Long,
    val reviewId: Long?,
    val likes: Int,
    val dislikes: Int,
    val url: String
)