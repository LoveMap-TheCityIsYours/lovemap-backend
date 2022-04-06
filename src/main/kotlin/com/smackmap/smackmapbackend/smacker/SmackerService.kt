package com.smackmap.smackmapbackend.smacker

import org.springframework.stereotype.Service

@Service
class SmackerService(private val smackerRepository: SmackerRepository) {

    fun createSmacker(createSmackerRequest: CreateSmackerRequest): SmackerResponse {

        TODO("Not yet implemented")
    }
}