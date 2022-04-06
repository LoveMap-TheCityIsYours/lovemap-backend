package com.smackmap.smackmapbackend.smacker

data class CreateSmackerRequest(
    private val userName: String,
    private val password: String,
    private val email: String
)

data class SmackerResponse(
    private val userName: String,
    private val email: String,
)