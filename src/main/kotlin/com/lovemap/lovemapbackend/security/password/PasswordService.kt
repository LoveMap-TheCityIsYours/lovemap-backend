package com.lovemap.lovemapbackend.security.password

import com.lovemap.lovemapbackend.lover.Lover
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.server.ResponseStatusException

@Service
@Transactional
class PasswordService(
    private val passwordRepository: PasswordRepository
) {
    suspend fun save(password: Password): Password {
        return passwordRepository.save(password)
    }

    suspend fun getPasswordOfLover(lover: Lover): Password {
        return passwordRepository.findByLoverId(lover.id)
            ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "User not found with ID '${lover.id}")
    }
}