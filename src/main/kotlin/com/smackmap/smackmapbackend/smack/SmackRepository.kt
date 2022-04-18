package com.smackmap.smackmapbackend.smack

import org.springframework.data.repository.kotlin.CoroutineCrudRepository

interface SmackRepository : CoroutineCrudRepository<Smack, Long> {
}