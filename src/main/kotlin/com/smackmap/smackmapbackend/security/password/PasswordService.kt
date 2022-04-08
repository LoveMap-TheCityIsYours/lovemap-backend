package com.smackmap.smackmapbackend.security.password

import com.smackmap.smackmapbackend.smacker.Smacker
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class PasswordService(
    private val passwordRepository: PasswordRepository
) {
    fun save(password: Password): Password {
        return passwordRepository.save(password)
    }

    fun getPasswordOfSmacker(smacker: Smacker): Password {
        return passwordRepository.findBySmacker(smacker)
    }
}