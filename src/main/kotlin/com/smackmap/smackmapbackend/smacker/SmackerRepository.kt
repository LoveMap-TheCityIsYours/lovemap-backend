package com.smackmap.smackmapbackend.smacker

import org.springframework.data.repository.kotlin.CoroutineCrudRepository

interface SmackerRepository : CoroutineCrudRepository<Smacker, Long> {
    suspend fun findByUserName(userName: String): Smacker?
    suspend fun findByEmail(email: String): Smacker?
    suspend fun findByLink(link: String): Smacker?
}