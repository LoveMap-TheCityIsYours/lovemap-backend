package com.lovemap.lovemapbackend.lover.points

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding

@ConstructorBinding
@ConfigurationProperties(prefix = "lovemap.lover.points")
data class LoverPoints(
    val reviewSubmitted: Int,
    val reviewReceived4Stars: Int,
    val reviewReceived5Stars: Int,
    val reportSubmitted: Int,
    val reportReceived: Int,
    val loveSpotAdded: Int,
    val loveMade: Int,
)
