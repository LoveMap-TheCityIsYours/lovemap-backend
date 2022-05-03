package com.smackmap.smackmapbackend.smackspot.risk

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding

@ConstructorBinding
@ConfigurationProperties(prefix = "smackmap.smackspot.risks")
data class SmackSpotRisks(val levels: Int, val riskList: List<Risk>) {
    data class Risk(
        val level: Int,
        val nameEN: String,
        val nameHU: String
    )
}