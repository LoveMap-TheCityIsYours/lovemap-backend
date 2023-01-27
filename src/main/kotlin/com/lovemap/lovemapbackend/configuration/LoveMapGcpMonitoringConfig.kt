package com.lovemap.lovemapbackend.configuration

import com.google.api.gax.core.CredentialsProvider
import com.google.auth.oauth2.ServiceAccountCredentials
import com.google.cloud.spring.autoconfigure.metrics.GcpStackdriverPropertiesConfigAdapter
import org.springframework.boot.actuate.autoconfigure.metrics.export.stackdriver.StackdriverMetricsExportAutoConfiguration
import org.springframework.boot.actuate.autoconfigure.metrics.export.stackdriver.StackdriverProperties
import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.core.io.ResourceLoader

@Configuration
@Profile("!dev")
class LoveMapGcpMonitoringConfig(
    private val resourceLoader: ResourceLoader,
    private val stackdriverProperties: StackdriverProperties
) : GcpStackdriverPropertiesConfigAdapter(stackdriverProperties) {

    @Bean
    override fun credentials(): CredentialsProvider {
        return CredentialsProvider {
            val inputStream = resourceLoader.getResource("classpath:monitoring-account-key.json").inputStream
            ServiceAccountCredentials.fromStream(inputStream)
        }
    }

    override fun projectId(): String {
        return stackdriverProperties.projectId
    }
}

@Configuration
@Profile("dev")
@EnableAutoConfiguration(
    exclude = [StackdriverMetricsExportAutoConfiguration::class]
)
class DevMonitoringConfig {
}