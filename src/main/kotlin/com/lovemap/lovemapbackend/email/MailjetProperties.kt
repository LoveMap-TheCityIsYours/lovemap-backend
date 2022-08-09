package com.lovemap.lovemapbackend.email

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding

@ConstructorBinding
@ConfigurationProperties(prefix = "lovemap.mailjet")
data class MailjetProperties(
    val apiKey: String,
    val secretKey: String
)
