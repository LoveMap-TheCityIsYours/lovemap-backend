package com.lovemap.lovemapbackend.authentication.lover

import org.springframework.data.repository.kotlin.CoroutineCrudRepository

interface LoverAuthenticationRepository : CoroutineCrudRepository<LoverAuthentication, Long> {
    suspend fun findByLoverId(loverId: Long): LoverAuthentication?
    suspend fun findByFacebookId(facebookId: String): LoverAuthentication?
}