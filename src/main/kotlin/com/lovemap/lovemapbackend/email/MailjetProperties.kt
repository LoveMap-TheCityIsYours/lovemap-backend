package com.lovemap.lovemapbackend.email

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.bind.ConstructorBinding

@ConfigurationProperties(prefix = "lovemap.mailjet")
data class MailjetProperties @ConstructorBinding constructor(
    val apiKey: String,
    val secretKey: String
)
