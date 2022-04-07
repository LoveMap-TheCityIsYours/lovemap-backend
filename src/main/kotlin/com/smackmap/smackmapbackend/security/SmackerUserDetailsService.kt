package com.smackmap.smackmapbackend.security

import com.smackmap.smackmapbackend.smacker.SmackerService
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.stereotype.Service

@Service
class SmackerUserDetailsService(
    private val smackerService: SmackerService,
) : UserDetailsService {

    override fun loadUserByUsername(username: String): UserDetails {
        val smacker = smackerService.getSmackerByUserName(username)
        val password = smackerService.getPasswordBySmacker(smacker)
        return SmackerUserDetails.of(smacker, password)
    }
}