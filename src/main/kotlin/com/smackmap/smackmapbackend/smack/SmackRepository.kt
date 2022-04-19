package com.smackmap.smackmapbackend.smack

import kotlinx.coroutines.flow.Flow
import org.springframework.data.repository.kotlin.CoroutineCrudRepository

interface SmackRepository : CoroutineCrudRepository<Smack, Long> {

    fun findDistinctBySmackerIdOrSmackerPartnerId(smackerId: Long, smackerPartnerId: Long): Flow<Smack>
}