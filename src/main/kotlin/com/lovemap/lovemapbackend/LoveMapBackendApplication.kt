package com.lovemap.lovemapbackend

import com.lovemap.lovemapbackend.authentication.facebook.FacebookProperties
import com.lovemap.lovemapbackend.authentication.security.LoveMapClients
import com.lovemap.lovemapbackend.email.MailjetProperties
import com.lovemap.lovemapbackend.lover.points.LoverPoints
import com.lovemap.lovemapbackend.lover.ranks.LoverRanks
import com.lovemap.lovemapbackend.lovespot.risk.LoveSpotRisks
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.runApplication
import org.springframework.web.reactive.config.EnableWebFlux

@EnableWebFlux
@SpringBootApplication
@EnableConfigurationProperties(
    value = [
        LoverRanks::class,
        LoveSpotRisks::class,
        LoverPoints::class,
        LoveMapClients::class,
        MailjetProperties::class,
        FacebookProperties::class
    ]
)
class LoveMapBackendApplication

fun main(args: Array<String>) {
    println("Number of CPU Cores: " + Runtime.getRuntime().availableProcessors())
    runApplication<LoveMapBackendApplication>(*args)
}
