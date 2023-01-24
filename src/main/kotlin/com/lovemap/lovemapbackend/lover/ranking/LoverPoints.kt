package com.lovemap.lovemapbackend.lover.ranking

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.bind.ConstructorBinding

@ConfigurationProperties(prefix = "lovemap.lover.points")
data class LoverPoints @ConstructorBinding constructor(
    val reviewSubmitted: Int,
    val reviewReceived4Stars: Int,
    val reviewReceived5Stars: Int,
    val reportSubmitted: Int,
    val reportReceived: Int,
    val loveSpotAdded: Int,
    val loveMade: Int,
    val photoUploaded: Int,
    val photoLikeReceived: Int,
    val photoDislikeReceived: Int,
    val pointsForFollower: Int,
)
