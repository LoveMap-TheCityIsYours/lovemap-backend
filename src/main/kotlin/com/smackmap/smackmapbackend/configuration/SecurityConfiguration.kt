package com.smackmap.smackmapbackend.configuration

import com.smackmap.smackmapbackend.security.JwtAuthenticationFilter
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.security.authentication.ReactiveAuthenticationManager
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
    private val jwtAuthenticationFilter: JwtAuthenticationFilter,
    private val smackerUserDetailsService: ReactiveUserDetailsService,
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
            .pathMatchers("/v3/api-docs/**").permitAll()
            .pathMatchers("/registration/**").permitAll()
            .pathMatchers("/smacker").hasRole("USER")
            .anyExchange().authenticated()
            .and()
            .addFilterAt(jwtAuthenticationFilter, SecurityWebFiltersOrder.HTTP_BASIC)
            .authenticationManager(reactiveAuthenticationManager())
            .securityContextRepository(NoOpServerSecurityContextRepository.getInstance())
        return http.build()
    }

    @Bean
    fun reactiveAuthenticationManager(): ReactiveAuthenticationManager {
        val authenticationManager = UserDetailsRepositoryReactiveAuthenticationManager(smackerUserDetailsService)
        authenticationManager.setPasswordEncoder(passwordEncoder)
        return authenticationManager
    }

    override fun addCorsMappings(registry: CorsRegistry) {
        registry.addMapping("/**").allowedOrigins("*").allowedMethods("*").allowedHeaders("*");
    }
}