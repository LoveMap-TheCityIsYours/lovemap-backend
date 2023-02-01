package com.lovemap.lovemapbackend.configuration

import com.lovemap.lovemapbackend.authentication.facebook.FacebookAuthenticationManager
import com.lovemap.lovemapbackend.authentication.facebook.FacebookProperties
import com.lovemap.lovemapbackend.authentication.security.JwtAuthenticationFilter
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.security.authentication.DelegatingReactiveAuthenticationManager
import org.springframework.security.authentication.UserDetailsRepositoryReactiveAuthenticationManager
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity
import org.springframework.security.config.web.server.SecurityWebFiltersOrder
import org.springframework.security.config.web.server.ServerHttpSecurity
import org.springframework.security.core.userdetails.ReactiveUserDetailsService
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.server.SecurityWebFilterChain
import org.springframework.security.web.server.context.NoOpServerSecurityContextRepository
import org.springframework.web.reactive.config.CorsRegistry
import org.springframework.web.reactive.config.WebFluxConfigurer

@Configuration
@EnableWebFluxSecurity
class SecurityConfiguration(
    @Value("\${lovemap.admins.emails}")
    private val adminEmails: List<String>,
    private val facebookProperties: FacebookProperties,
    private val jwtAuthenticationFilter: JwtAuthenticationFilter,
    private val loverUserDetailsService: ReactiveUserDetailsService,
    private val passwordEncoder: PasswordEncoder
) : WebFluxConfigurer {

    @Bean
    @Order(Ordered.HIGHEST_PRECEDENCE)
    fun apiHttpSecurity(http: ServerHttpSecurity): SecurityWebFilterChain {
        http.formLogin().disable()
            .httpBasic().disable()
            .csrf().disable()
            .authorizeExchange()
            .pathMatchers("/webjars/swagger-ui/**").permitAll()
            .pathMatchers("/swagger-ui/**").permitAll()
            .pathMatchers("/v3/api-docs/**").permitAll()
            .pathMatchers("/authentication/**").permitAll()
            .pathMatchers("/debug/**").permitAll()
            .pathMatchers("/actuator/health/**").permitAll()
            .pathMatchers("/actuator/info/**").permitAll()
            .pathMatchers("/actuator/metrics/**").permitAll()
            .pathMatchers("/actuator/prometheus/**").permitAll()
            .pathMatchers("/lover-locator/**").permitAll()

            .pathMatchers("/join-us/**").permitAll()
            .pathMatchers("/join-us.**").permitAll()
            .pathMatchers("/privacy-policy.html").permitAll()
            .pathMatchers("/terms-of-use.html").permitAll()
            .pathMatchers("/.well-known/assetlinks.json").permitAll()
            .pathMatchers("/favicon.ico").permitAll()
            .pathMatchers("/app-ads.txt").permitAll()

            .pathMatchers("/love/**").hasRole("USER")
            .pathMatchers("/lover/**").hasRole("USER")
            .pathMatchers("/lovers/**").hasRole("USER")
            .pathMatchers("/v2/lovers/**").hasRole("USER")
            .pathMatchers("/lovespots/**").hasRole("USER")
            .pathMatchers("/relation/**").hasRole("USER")
            .pathMatchers("/partnership/**").hasRole("USER")
            .pathMatchers("/geolocations/**").hasRole("USER")
            .pathMatchers("/photos/**").hasRole("USER")
            .pathMatchers("/newsfeed/**").hasRole("USER")

            .pathMatchers("/admin/**").hasRole("ADMIN")
            .pathMatchers("/actuator/**").hasRole("ADMIN")
            .anyExchange().authenticated()
            .and()
            .addFilterAt(jwtAuthenticationFilter, SecurityWebFiltersOrder.HTTP_BASIC)
            .authenticationManager(authenticationManager())
            .securityContextRepository(NoOpServerSecurityContextRepository.getInstance())
        return http.build()
    }

    @Bean
    fun userDetailsRepositoryReactiveAuthenticationManager(): UserDetailsRepositoryReactiveAuthenticationManager {
        val authenticationManager = UserDetailsRepositoryReactiveAuthenticationManager(loverUserDetailsService)
        authenticationManager.setPasswordEncoder(passwordEncoder)
        return authenticationManager
    }

    @Bean
    fun facebookAuthenticationManager(): FacebookAuthenticationManager {
        return FacebookAuthenticationManager(adminEmails, facebookProperties)
    }

    @Bean
    fun authenticationManager(): DelegatingReactiveAuthenticationManager {
        return DelegatingReactiveAuthenticationManager(
            mutableListOf(
                facebookAuthenticationManager(),
                userDetailsRepositoryReactiveAuthenticationManager()
            )
        )
    }

    override fun addCorsMappings(registry: CorsRegistry) {
        registry.addMapping("/**").allowedOrigins("*").allowedMethods("*").allowedHeaders("*");
    }
}