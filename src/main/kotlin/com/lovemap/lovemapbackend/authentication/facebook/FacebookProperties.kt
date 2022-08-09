package com.lovemap.lovemapbackend.authentication.facebook

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding

@ConstructorBinding
@ConfigurationProperties(prefix = "lovemap.facebook")
data class FacebookProperties(
    val appId: String,
    val appSecret: String
)