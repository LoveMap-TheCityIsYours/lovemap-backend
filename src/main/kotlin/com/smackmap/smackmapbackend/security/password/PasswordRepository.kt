package com.smackmap.smackmapbackend.security.password

import com.smackmap.smackmapbackend.smacker.Smacker
import org.springframework.data.jpa.repository.JpaRepository

interface PasswordRepository : JpaRepository<Password, Long> {
    fun findBySmacker(smacker: Smacker): Password
}