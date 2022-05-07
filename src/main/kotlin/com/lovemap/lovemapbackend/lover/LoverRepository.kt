package com.lovemap.lovemapbackend.lover

import org.springframework.data.repository.kotlin.CoroutineCrudRepository

interface LoverRepository : CoroutineCrudRepository<Lover, Long> {
    suspend fun findByUserName(userName: String): Lover?
    suspend fun findByEmail(email: String): Lover?
    suspend fun findByLink(link: String): Lover?
}