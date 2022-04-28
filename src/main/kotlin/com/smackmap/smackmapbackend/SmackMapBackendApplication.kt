package com.smackmap.smackmapbackend

import com.smackmap.smackmapbackend.smacker.ranks.SmackerRanks
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.runApplication
import org.springframework.web.reactive.config.EnableWebFlux

@EnableWebFlux
@SpringBootApplication
@EnableConfigurationProperties(value = [SmackerRanks::class])
class SmackMapBackendApplication

fun main(args: Array<String>) {
    println("Number of CPU Cores: " + Runtime.getRuntime().availableProcessors())
    runApplication<SmackMapBackendApplication>(*args)
}
