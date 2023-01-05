package com.lovemap.lovemapbackend.configuration

import com.google.auth.Credentials
import com.google.auth.oauth2.ServiceAccountCredentials
import com.google.maps.GeoApiContext
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.context.properties.bind.ConstructorBinding
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.io.ResourceLoader

@Configuration
@EnableConfigurationProperties(GoogleConfigProperties::class)
class GoogleCloudConfiguration(
    private val googleConfigProperties: GoogleConfigProperties,
    private val resourceLoader: ResourceLoader
) {

    @Bean
    fun geoApiContext(): GeoApiContext {
        return GeoApiContext.Builder()
            .apiKey(googleConfigProperties.apiKey)
            .build()
    }


    @Bean
    fun googleCredentials(): Credentials {
        val inputStream = resourceLoader.getResource("classpath:bucket-writer-key.json").inputStream
        return ServiceAccountCredentials.fromStream(inputStream)
    }
}

@ConfigurationProperties(prefix = "lovemap.google")
data class GoogleConfigProperties @ConstructorBinding constructor(
    val apiKey: String,
    val projectId: String,
    val publicPhotosBucket: String
)
