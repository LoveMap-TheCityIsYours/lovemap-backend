package com.lovemap.lovemapbackend.authentication.facebook

import com.fasterxml.jackson.databind.JsonNode
import com.lovemap.lovemapbackend.authentication.security.AUTHORITY_ADMIN
import com.lovemap.lovemapbackend.authentication.security.AUTHORITY_USER
import com.lovemap.lovemapbackend.utils.ErrorCode
import com.lovemap.lovemapbackend.utils.LoveMapException
import kotlinx.coroutines.reactor.awaitSingle
import kotlinx.coroutines.reactor.mono
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.authentication.ReactiveAuthenticationManager
import org.springframework.security.core.Authentication
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.util.DefaultUriBuilderFactory
import reactor.core.publisher.Mono
import java.net.URI

class FacebookAuthenticationManager(
    private val adminEmails: List<String>,
    private val facebookProperties: FacebookProperties,
) : ReactiveAuthenticationManager {

    private val webClient: WebClient = WebClient.create()

    override fun authenticate(authentication: Authentication): Mono<Authentication> {
        return if (authentication is FacebookAuthenticationToken) {
            mono {
                val fbAccessToken = authentication.credentials as String
                val tokenValidationUri = buildValidationUri(fbAccessToken)
                val validationResponse: ResponseEntity<JsonNode> = getValidationFromFacebook(tokenValidationUri)
                authenticateUser(authentication, validationResponse, fbAccessToken)
            }
        } else {
            Mono.empty()
        }
    }

    private fun buildValidationUri(fbAccessToken: String): URI {
        val tokenValidationUriBuilder = DefaultUriBuilderFactory().builder()
        return tokenValidationUriBuilder.scheme("https").host("graph.facebook.com")
            .path("debug_token")
            .queryParam("input_token", fbAccessToken)
            .queryParam("access_token", "${facebookProperties.appId}|${facebookProperties.appSecret}")
            .build()
    }

    private suspend fun getValidationFromFacebook(tokenValidationUri: URI): ResponseEntity<JsonNode> {
        return webClient.get()
            .uri(tokenValidationUri)
            .retrieve()
            .toEntity(JsonNode::class.java)
            .doOnError { throw LoveMapException(HttpStatus.FORBIDDEN, ErrorCode.FacebookLoginFailed) }
            .awaitSingle()
    }

    private fun authenticateUser(
        authentication: FacebookAuthenticationToken,
        validationResponse: ResponseEntity<JsonNode>,
        fbAccessToken: String
    ): FacebookAuthenticationToken {
        val facebookId = authentication.principal as String
        return if (facebookIdMatches(facebookId, validationResponse)) {
            val userName = authentication.name
            val email = authentication.details as String
            FacebookAuthenticationToken(
                userName = userName,
                email = email,
                fbAccessToken = fbAccessToken,
                facebookId = facebookId,
                isAuthenticated = true,
                authorities = getGrantedAuthorities(email)
            )
        } else {
            throw LoveMapException(HttpStatus.FORBIDDEN, ErrorCode.FacebookLoginFailed)
        }
    }

    private fun facebookIdMatches(
        facebookId: String,
        validationResponse: ResponseEntity<JsonNode>
    ) = facebookId == validationResponse.body?.findValue("data")?.findValue("user_id")?.textValue()

    private fun getGrantedAuthorities(email: String): MutableList<SimpleGrantedAuthority> {
        val authorities = ArrayList<String>()
        authorities.add(AUTHORITY_USER)
        if (adminEmails.contains(email)) {
            authorities.add(AUTHORITY_ADMIN)
        }
        return authorities.map { SimpleGrantedAuthority(it) }.toMutableList()
    }
}