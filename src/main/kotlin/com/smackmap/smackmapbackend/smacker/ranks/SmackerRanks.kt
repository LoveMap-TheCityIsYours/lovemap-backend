package com.smackmap.smackmapbackend.smacker.ranks

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding

@ConstructorBinding
@ConfigurationProperties(prefix = "smackmap.smacker.ranks")
data class SmackerRanks(val rankList: List<Rank>) {
    data class Rank(
        val rank: Int,
        val nameEN: String,
        val nameHU: String,
        val pointsNeeded: Int
    )
}
