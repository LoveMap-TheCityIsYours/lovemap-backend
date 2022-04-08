package com.smackmap.smackmapbackend.configuration

import com.smackmap.smackmapbackend.security.JwtAuthenticationFilter
import com.smackmap.smackmapbackend.security.SmackerUserDetailsService
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.core.AuthenticationException
import org.springframework.security.crypto.factory.PasswordEncoderFactories
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.UrlBasedCorsConfigurationSource
import org.springframework.web.filter.CorsFilter
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse


@Configuration
@EnableWebSecurity
class SecurityConfiguration(
    private val smackerUserDetailsService: SmackerUserDetailsService,
    private val jwtAuthenticationFilter: JwtAuthenticationFilter,
) : WebSecurityConfigurerAdapter(false) {

    override fun configure(httpSecurity: HttpSecurity) {
        // Enable CORS and disable CSRF
        httpSecurity.cors().and().csrf().disable()

        // Set session management to stateless
        httpSecurity
            .sessionManagement()
            .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            .and()

        // Set unauthorized requests exception handler
        httpSecurity
            .exceptionHandling()
            .authenticationEntryPoint { _: HttpServletRequest,
                                        response: HttpServletResponse,
                                        ex: AuthenticationException ->
                response.sendError(
                    HttpServletResponse.SC_UNAUTHORIZED,
                    ex.message
                )
            }
            .and()

        // TODO: proper endpoint permissions
        // Set permissions on endpoints
        httpSecurity.authorizeRequests() // Our public endpoints
            .antMatchers("/**").permitAll()
            .antMatchers(HttpMethod.POST, "/api/book/search").permitAll() // Our private endpoints
            .anyRequest().authenticated()

        // Add JWT token filter
        httpSecurity.addFilterBefore(
            jwtAuthenticationFilter,
            UsernamePasswordAuthenticationFilter::class.java
        )

        httpSecurity
            .csrf()
            .disable()
            .authorizeRequests()
            .antMatchers("/**")
            .permitAll()
            .anyRequest()
            .anonymous()
    }

    override fun configure(auth: AuthenticationManagerBuilder) {
        auth.userDetailsService(smackerUserDetailsService)
    }

    @Bean
    override fun authenticationManagerBean(): AuthenticationManager {
        return super.authenticationManagerBean()
    }

    @Bean
    fun passwordEncoder(): PasswordEncoder {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder()
    }

    // Used by spring security if CORS is enabled.
    @Bean
    fun corsFilter(): CorsFilter {
        // TODO: it has reactive version
        val source = UrlBasedCorsConfigurationSource()
        val config = CorsConfiguration()
        config.allowCredentials = true
        config.addAllowedOrigin("*")
        config.addAllowedHeader("*")
        config.addAllowedMethod("*")
        source.registerCorsConfiguration("/**", config)
        return CorsFilter(source)
    }
}