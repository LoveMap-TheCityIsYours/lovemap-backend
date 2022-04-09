package com.smackmap.smackmapbackend.security

import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Component

@Component
class SmackerPasswordEncoder : PasswordEncoder {
    override fun encode(rawPassword: CharSequence?): String {
        TODO("Not yet implemented")
    }

    override fun matches(rawPassword: CharSequence?, encodedPassword: String?): Boolean {
        TODO("Not yet implemented")
    }
}