package com.smackmap.smackmapbackend.security

import com.smackmap.smackmapbackend.smacker.Password
import com.smackmap.smackmapbackend.smacker.Smacker
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.UserDetails

data class SmackerUserDetails(
    private val userName: String,
    private val password: String
) : UserDetails {

    companion object {
        fun of(smacker: Smacker, password: Password): SmackerUserDetails {
            return SmackerUserDetails(smacker.userName, password.passwordHash)
        }
    }

    override fun getAuthorities(): MutableCollection<out GrantedAuthority> {
        // TODO: come up with proper authorities
        return mutableListOf(SimpleGrantedAuthority("ROLE_USER"))
    }

    override fun getPassword(): String = password
    override fun getUsername(): String = userName

    override fun isAccountNonExpired(): Boolean = true
    override fun isAccountNonLocked(): Boolean = true
    override fun isCredentialsNonExpired(): Boolean = true
    override fun isEnabled(): Boolean = true
}