package com.smackmap.smackmapbackend.smacker

import org.springframework.data.jpa.repository.JpaRepository

interface SmackerRepository : JpaRepository<Smacker, Long> {
    fun findByUserName(userName: String): Smacker?
    fun findByEmail(email: String): Smacker?
    fun findByLink(link: String): Smacker?
}