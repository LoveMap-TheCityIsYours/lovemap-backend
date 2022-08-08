package com.lovemap.lovemapbackend.authentication.security

import com.lovemap.lovemapbackend.authentication.lover.LoverAuthentication
import com.lovemap.lovemapbackend.lover.Lover
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.UserDetails

data class LoverUserDetails(
    private val userName: String,
    private val password: String?,
    private val authorities: List<String>,
) : UserDetails {

    companion object {
        fun of(lover: Lover, password: LoverAuthentication, authorities: List<String>): LoverUserDetails {
            return LoverUserDetails(lover.userName, password.passwordHash, authorities)
        }
    }

    override fun getAuthorities(): MutableCollection<out GrantedAuthority> {
        return authorities.map { SimpleGrantedAuthority(it) }.toMutableList()
    }

    override fun getPassword(): String? = password
    override fun getUsername(): String = userName

    override fun isAccountNonExpired(): Boolean = true
    override fun isAccountNonLocked(): Boolean = true
    override fun isCredentialsNonExpired(): Boolean = true
    override fun isEnabled(): Boolean = true
}