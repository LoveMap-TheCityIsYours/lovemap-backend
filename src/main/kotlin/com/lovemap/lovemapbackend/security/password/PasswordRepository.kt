package com.lovemap.lovemapbackend.security.password

import org.springframework.data.repository.kotlin.CoroutineCrudRepository

interface PasswordRepository : CoroutineCrudRepository<Password, Long> {
    suspend fun findByLoverId(loverId: Long): Password?
}