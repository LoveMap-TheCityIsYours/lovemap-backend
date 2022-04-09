package com.smackmap.smackmapbackend.configuration

import com.smackmap.smackmapbackend.security.JwtAuthenticationFilter
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.security.authentication.ReactiveAuthenticationManager
import org.springframework.security.authentication.UserDetailsRepositoryReactiveAuthenticationManager
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity
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
// TODO: is it needed?
//@EnableReactiveMethodSecurity
class SecurityConfiguration(
    private val jwtAuthenticationFilter: JwtAuthenticationFilter,
    private val smackerUserDetailsService: ReactiveUserDetailsService,
    private val passwordEncoder: PasswordEncoder
//    private val authenticationManager: ReactiveAuthenticationManager
) : WebFluxConfigurer {
//    : WebSecurityConfigurerAdapter(false) {

    @Bean
    @Order(Ordered.HIGHEST_PRECEDENCE)
    fun apiHttpSecurity(http: ServerHttpSecurity): SecurityWebFilterChain {
        http.formLogin().disable()
            .httpBasic().disable()
            .csrf().disable()
            .authorizeExchange()
            .pathMatchers("/auth/**").permitAll()
            .pathMatchers("/swagger-ui/**").permitAll()
            .pathMatchers("/v3/api-docs/**").permitAll()
            .pathMatchers("/smacker").hasRole("USER")
            .anyExchange().authenticated()
            .and()
            .addFilterAt(jwtAuthenticationFilter, SecurityWebFiltersOrder.HTTP_BASIC)
            // TODO: needed?
//            .oauth2ResourceServer { obj: OAuth2ResourceServerSpec -> obj.jwt() }
            .authenticationManager(reactiveAuthenticationManager())
//                TODO: not sure if its needed:
            .securityContextRepository(NoOpServerSecurityContextRepository.getInstance())
//            .securityContextRepository()
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

    //    @Bean
//    fun publicApiHttpSecurity(http: ServerHttpSecurity): SecurityWebFilterChain {
//        http.formLogin().disable()
//            .httpBasic().disable()
//            .csrf().disable()
//            .authorizeExchange()
//            .pathMatchers("/auth/**").permitAll()
//            .pathMatchers("/swagger-ui/**").permitAll()
//            .pathMatchers("/v3/api-docs/**").permitAll()
//            .anyExchange().permitAll()
//            .and()
//            .anonymous()
//        return http.build()
//    }

//    fun configure(httpSecurity: ServerHttpSecurity) {
//        httpSecurity.cors().and().csrf().disable()
//            .sessionManagement()
//            .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
//            .and()
//            .exceptionHandling()
//            .authenticationEntryPoint { _: HttpServletRequest,
//                                        response: HttpServletResponse,
//                                        ex: AuthenticationException ->
//                response.sendError(
//                    HttpServletResponse.SC_UNAUTHORIZED,
//                    ex.message
//                )
//            }
//            .and()
//            .authorizeRequests() // Our public endpoints
//            .antMatchers("/auth/**").permitAll()
//            .antMatchers("/swagger-ui/**").permitAll()
//            .antMatchers("/v3/api-docs/**").permitAll()
//            .antMatchers("/smacker").hasRole("USER")
//            .anyRequest().authenticated()
//            .and()
//            .userDetailsService(smackerUserDetailsService)
//            .addFilterBefore(
//                jwtAuthenticationFilter,
//                UsernamePasswordAuthenticationFilter::class.java
//            )
//
////        httpSecurity
////            .csrf()
////            .disable()
////            .authorizeRequests()
////            .antMatchers("/**")
////            .permitAll()
////            .anyRequest()
////            .anonymous()
//    }
}