package com.smackmap.smackmapbackend.security

import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jws
import io.jsonwebtoken.JwtException
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import io.jsonwebtoken.security.Keys
import mu.KotlinLogging
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
import java.time.ZoneOffset
import java.util.Date
import java.util.stream.Collectors

@Service
class JwtService {
    val logger = KotlinLogging.logger {}

    private val HEADER_PREFIX = "Bearer "
    private val AUTHORITIES_KEY = "roles"
    private val SECRET = "smackmap-secret-key-aaaaaaaaaaaaaa"
    private val SECRET_KEY = Keys.hmacShaKeyFor(SECRET.toByteArray(UTF_8))

    fun generateToken(authentication: Authentication): String? {
        val username = authentication.name
        val authorities = authentication.authorities
        val claims = Jwts.claims().setSubject(username)
        if (!authorities.isEmpty()) {
            claims[AUTHORITIES_KEY] =
                authorities.stream().map { obj: GrantedAuthority -> obj.authority }
                    .collect(Collectors.joining(","))
        }
        val now = Date()
        return Jwts.builder()
            .setClaims(claims)
            .setIssuedAt(now)
            .setExpiration(Date(LocalDateTime.now().plusYears(10).toEpochSecond(ZoneOffset.UTC)))
            .signWith(SECRET_KEY, SignatureAlgorithm.HS256)
            .compact()
    }

    fun getAuthentication(token: String): Authentication {
        val claims: Claims = Jwts.parserBuilder().setSigningKey(SECRET_KEY).build().parseClaimsJws(token).getBody()
        val authoritiesClaim = claims[AUTHORITIES_KEY]
        val authorities: Collection<GrantedAuthority> =
            if (authoritiesClaim == null) AuthorityUtils.NO_AUTHORITIES else AuthorityUtils.commaSeparatedStringToAuthorityList(
                authoritiesClaim.toString()
            )
        val principal = User(claims.subject, "", authorities)
        return UsernamePasswordAuthenticationToken(principal, token, authorities)
    }

    fun validateToken(token: String): Boolean {
        try {
            val claims: Jws<Claims> = Jwts
                .parserBuilder().setSigningKey(SECRET_KEY).build()
                .parseClaimsJws(token)
            //  parseClaimsJws will check expiration date. No need do here.
            logger.info("expiration date: {}", claims.body.expiration)
            return true
        } catch (e: JwtException) {
            logger.info("Invalid JWT token: {}", e.message)
            logger.trace("Invalid JWT token trace.", e)
        } catch (e: IllegalArgumentException) {
            logger.info("Invalid JWT token: {}", e.message)
            logger.trace("Invalid JWT token trace.", e)
        }
        return false
    }

    fun resolveToken(request: ServerHttpRequest): String? {
        val bearerToken = request.headers.getFirst(HttpHeaders.AUTHORIZATION)
        return bearerToken?.substringAfter(HEADER_PREFIX)
    }
}