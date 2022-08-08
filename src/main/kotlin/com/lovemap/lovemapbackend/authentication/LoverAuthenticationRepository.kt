package com.lovemap.lovemapbackend.authentication

import org.springframework.data.repository.kotlin.CoroutineCrudRepository

interface LoverAuthenticationRepository : CoroutineCrudRepository<LoverAuthentication, Long> {
    suspend fun findByLoverId(loverId: Long): LoverAuthentication?
}