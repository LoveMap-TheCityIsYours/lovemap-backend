package com.lovemap.lovemapbackend

import com.lovemap.lovemapbackend.authentication.facebook.FacebookProperties
import com.lovemap.lovemapbackend.authentication.security.LoveMapClients
import com.lovemap.lovemapbackend.email.MailjetProperties
import com.lovemap.lovemapbackend.lover.ranking.LoverPoints
import com.lovemap.lovemapbackend.lover.ranking.LoverRanks
import com.lovemap.lovemapbackend.lovespot.risk.LoveSpotRisks
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.runApplication
import org.springframework.data.mongodb.repository.config.EnableReactiveMongoRepositories
import org.springframework.data.r2dbc.repository.config.EnableR2dbcRepositories
import org.springframework.scheduling.annotation.EnableScheduling
import org.springframework.web.reactive.config.EnableWebFlux

@EnableWebFlux
@EnableScheduling
@SpringBootApplication
@EnableR2dbcRepositories
@EnableReactiveMongoRepositories
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
