package com.smackmap.smackmapbackend.security

import com.smackmap.smackmapbackend.security.password.PasswordService
import com.smackmap.smackmapbackend.smacker.SmackerService
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.stereotype.Service

@Service
class SmackerUserDetailsService(
    private val smackerService: SmackerService,
    private val passwordService: PasswordService
) : UserDetailsService {

    override fun loadUserByUsername(username: String): UserDetails {
        val smacker = smackerService.getByUserName(username)
        val password = passwordService.getPasswordOfSmacker(smacker)
        return SmackerUserDetails.of(smacker, password)
    }
}