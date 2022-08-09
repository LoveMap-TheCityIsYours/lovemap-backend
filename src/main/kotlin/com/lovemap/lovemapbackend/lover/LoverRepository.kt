package com.lovemap.lovemapbackend.lover

import org.springframework.data.repository.kotlin.CoroutineCrudRepository

interface LoverRepository : CoroutineCrudRepository<Lover, Long> {
    suspend fun findByUserName(userName: String): Lover?
    suspend fun findByEmail(email: String): Lover?
    suspend fun findByUuid(uuid: String): Lover?
    suspend fun existsByEmail(email: String): Boolean
}