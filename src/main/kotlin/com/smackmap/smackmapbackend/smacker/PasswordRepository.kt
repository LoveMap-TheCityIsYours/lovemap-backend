package com.smackmap.smackmapbackend.smacker

import org.springframework.data.jpa.repository.JpaRepository

interface PasswordRepository : JpaRepository<Password, Long> {
    fun findBySmacker(smacker: Smacker): Password
}