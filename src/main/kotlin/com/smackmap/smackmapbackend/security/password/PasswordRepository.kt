package com.smackmap.smackmapbackend.security.password

import org.springframework.data.repository.kotlin.CoroutineCrudRepository

interface PasswordRepository : CoroutineCrudRepository<Password, Long> {
    suspend fun findBySmackerId(smackerId: Long): Password?
}