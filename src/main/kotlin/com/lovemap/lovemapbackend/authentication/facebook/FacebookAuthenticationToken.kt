package com.lovemap.lovemapbackend.authentication.facebook

import org.springframework.security.core.Authentication
import org.springframework.security.core.GrantedAuthority

class FacebookAuthenticationToken(
    private val email: String,
    private val facebookId: String,
    private val fbAccessToken: String,
    private var isAuthenticated: Boolean = false,
    private val authorities: MutableCollection<out GrantedAuthority> = mutableListOf()
) : Authentication {

    override fun getName(): String {
        return email
    }

    override fun getAuthorities(): MutableCollection<out GrantedAuthority> {
        return authorities
    }

    override fun getCredentials(): Any {
        return fbAccessToken
    }

    override fun getDetails(): Any {
        return email
    }

    override fun getPrincipal(): Any {
        return facebookId
    }

    override fun isAuthenticated(): Boolean {
        return isAuthenticated
    }

    override fun setAuthenticated(isAuthenticated: Boolean) {
        this.isAuthenticated = isAuthenticated
    }
}