package com.lovemap.lovemapbackend.authentication.facebook

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.bind.ConstructorBinding

@ConfigurationProperties(prefix = "lovemap.facebook")
data class FacebookProperties @ConstructorBinding constructor(
    val appId: String,
    val appSecret: String
)