package com.smackmap.smackmapbackend

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.web.reactive.config.EnableWebFlux

@EnableWebFlux
@SpringBootApplication
class SmackMapBackendApplication

fun main(args: Array<String>) {
    runApplication<SmackMapBackendApplication>(*args)
}
