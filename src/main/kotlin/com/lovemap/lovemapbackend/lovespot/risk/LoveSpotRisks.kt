package com.lovemap.lovemapbackend.lovespot.risk

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.bind.ConstructorBinding

@ConfigurationProperties(prefix = "lovemap.lovespot.risks")
data class LoveSpotRisks @ConstructorBinding constructor(
    val levels: Int,
    val riskList: List<Risk>
) {
    data class Risk(
        val level: Int,
        val nameEN: String,
        val nameHU: String
    )
}