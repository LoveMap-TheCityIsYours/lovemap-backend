package com.lovemap.lovemapbackend.configuration

import kotlinx.coroutines.ExecutorCoroutineDispatcher
import kotlinx.coroutines.asCoroutineDispatcher
import org.flywaydb.core.Flyway
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.flyway.FlywayProperties
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.crypto.factory.PasswordEncoderFactories
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean
import java.util.concurrent.Executors
import javax.validation.Validator


@Configuration
@EnableConfigurationProperties(FlywayProperties::class)
class AppConfiguration(
    @Value("\${lovemap.google.apiKey}") private val googleApiKey: String
) {

    @Bean
    fun passwordEncoder(): PasswordEncoder {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder()
    }

    @Bean(initMethod = "migrate")
    fun flyway(flywayProperties: FlywayProperties): Flyway {
        return Flyway(
            Flyway.configure()
                .baselineOnMigrate(true)
                .dataSource(flywayProperties.url, flywayProperties.user, flywayProperties.password)
        )
    }

    @Bean
    fun validator(): Validator {
        return LocalValidatorFactoryBean()
    }

    @Bean
    fun blockingCoroutineDispatcher(): ExecutorCoroutineDispatcher {
        val threads = Runtime.getRuntime().availableProcessors() * 4
        return Executors.newFixedThreadPool(threads).asCoroutineDispatcher()
    }
}