package com.smackmap.smackmapbackend.security

import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.stereotype.Component
import java.io.Serializable
import java.util.Date
import java.util.function.Function

const val ACCESS_TOKEN_VALIDITY_SECONDS = (5 * 60 * 60).toLong()
// TODO: proper configurable signing key
const val SIGNING_KEY = "SmackMapDummySignKey"
const val TOKEN_PREFIX = "Bearer "

//@Component
class JwtTokenUtil : Serializable {

    fun getUsernameFromToken(token: String?): String {
        return getClaimFromToken(token) { obj: Claims -> obj.subject }
    }

    fun getExpirationDateFromToken(token: String?): Date {
        return getClaimFromToken(token) { obj: Claims -> obj.expiration }
    }

    fun <T> getClaimFromToken(token: String?, claimsResolver: Function<Claims, T>): T {
        val claims = getAllClaimsFromToken(token)
        return claimsResolver.apply(claims)
    }

    fun getAllClaimsFromToken(token: String?): Claims {
        return Jwts.parser()
            .setSigningKey(SIGNING_KEY)
            .parseClaimsJws(token)
            .body
    }

    fun isTokenExpired(token: String?): Boolean {
        val expiration = getExpirationDateFromToken(token)
        return expiration.before(Date())
    }

    fun generateToken(user: UserDetails): String {
        val claims = Jwts.claims().setSubject(user.username)
        claims["scopes"] = user.authorities
        return Jwts.builder()
            .setClaims(claims)
            .setIssuer("http://smackmap.com")
            .setIssuedAt(Date(System.currentTimeMillis()))
            .setExpiration(Date(System.currentTimeMillis() + ACCESS_TOKEN_VALIDITY_SECONDS * 1000))
                // TODO: better signing algo
            .signWith(SignatureAlgorithm.HS512, SIGNING_KEY)
            .compact()
    }

    fun validateTokenBetter(token: String?, userDetails: UserDetails): Boolean {
        val username = getUsernameFromToken(token)
        return username == userDetails.username && !isTokenExpired(token)
    }

    fun validateToken(token: String): Boolean {
        return !isTokenExpired(token)
    }
}