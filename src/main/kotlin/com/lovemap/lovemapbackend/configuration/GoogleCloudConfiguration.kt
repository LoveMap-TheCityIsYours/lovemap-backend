package com.lovemap.lovemapbackend.configuration

import com.google.auth.Credentials
import com.google.auth.oauth2.ServiceAccountCredentials
import com.google.maps.GeoApiContext
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.util.ResourceUtils
import java.io.FileInputStream

@Configuration
@EnableConfigurationProperties(GoogleConfigProperties::class)
class GoogleCloudConfiguration(
    private val googleConfigProperties: GoogleConfigProperties
) {

    @Bean
    fun geoApiContext(): GeoApiContext {
        return GeoApiContext.Builder()
            .apiKey(googleConfigProperties.apiKey)
            .build()
    }


    @Bean
    fun googleCredentials(): Credentials {
        val file = ResourceUtils.getFile("classpath:bucket-writer-key.json")
        return ServiceAccountCredentials.fromStream(FileInputStream(file))
    }
}

@ConstructorBinding
@ConfigurationProperties(prefix = "lovemap.google")
data class GoogleConfigProperties(
     val apiKey: String,
     val projectId: String,
     val publicPhotosBucket: String
)
