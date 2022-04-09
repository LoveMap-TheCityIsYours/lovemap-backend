package com.smackmap.smackmapbackend.security.password

import com.smackmap.smackmapbackend.smacker.Smacker
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class PasswordService(
    private val passwordRepository: PasswordRepository
) {
    suspend fun save(password: Password): Password {
        return passwordRepository.save(password)
    }

    suspend fun getPasswordOfSmacker(smacker: Smacker): Password {
        return passwordRepository.findBySmackerId(smacker.id)
    }
}