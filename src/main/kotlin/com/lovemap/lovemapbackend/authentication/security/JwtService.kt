package com.lovemap.lovemapbackend.authentication.security

import io.jsonwebtoken.*
import io.jsonwebtoken.security.Keys
import mu.KotlinLogging
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpHeaders
import org.springframework.http.server.reactive.ServerHttpRequest
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.AuthorityUtils
import org.springframework.security.core.userdetails.User
import org.springframework.stereotype.Service
import java.nio.charset.StandardCharsets.UTF_8
import java.time.LocalDateTime
import java.time.ZoneOffset.UTC
import java.util.*
import java.util.stream.Collectors

private const val HEADER_PREFIX = "Bearer "
private const val AUTHORITIES_KEY = "roles"

@Service
class JwtService(
    @Value("\${lovemap.signingKey}")
    private val signingKey: String
) {
    val logger = KotlinLogging.logger {}

    private val secretKey = Keys.hmacShaKeyFor(signingKey.toByteArray(UTF_8))

    fun generateToken(authentication: Authentication): String {
        val username = authentication.name
        val authorities = authentication.authorities
        val claims = Jwts.claims().setSubject(username)
        if (!authorities.isEmpty()) {
            claims[AUTHORITIES_KEY] =
                authorities.stream().map { obj: GrantedAuthority -> obj.authority }
                    .collect(Collectors.joining(","))
        }
        val now = Date()
        val expirationLocalDate = LocalDateTime.now().plusYears(10)
        val expirationDate = Date.from(expirationLocalDate.toInstant(UTC))
        val builder = Jwts.builder()
        builder
            .setClaims(claims)
            .setIssuedAt(now)
            .setExpiration(expirationDate)
            .signWith(secretKey, SignatureAlgorithm.HS256)
        return builder.compact()
    }

    fun getAuthentication(token: String): Authentication {
        val claims: Claims = Jwts.parserBuilder().setSigningKey(secretKey).build().parseClaimsJws(token).body
        val authoritiesClaim = claims[AUTHORITIES_KEY]
        val authorities: Collection<GrantedAuthority> = if (authoritiesClaim == null) {
            AuthorityUtils.NO_AUTHORITIES
        } else AuthorityUtils.commaSeparatedStringToAuthorityList(
            authoritiesClaim.toString()
        )
        val principal = User(claims.subject, "", authorities)
        return UsernamePasswordAuthenticationToken(principal, token, authorities)
    }

    fun validateToken(token: String): Boolean {
        try {
            val claims: Jws<Claims> = Jwts
                .parserBuilder().setSigningKey(secretKey).build()
                .parseClaimsJws(token)
            //  parseClaimsJws will check expiration date. No need do here.
            logger.debug("Token validated. Expiration date: {}", claims.body.expiration)
            return true
        } catch (e: JwtException) {
            logger.debug("Invalid JWT token: {}", e.message)
            logger.trace("Invalid JWT token trace.", e)
        } catch (e: IllegalArgumentException) {
            logger.debug("Invalid JWT token: {}", e.message)
            logger.trace("Invalid JWT token trace.", e)
        }
        return false
    }

    fun resolveToken(request: ServerHttpRequest): String? {
        val bearerToken = request.headers.getFirst(HttpHeaders.AUTHORIZATION)
        return bearerToken?.substringAfter(HEADER_PREFIX)
    }
}