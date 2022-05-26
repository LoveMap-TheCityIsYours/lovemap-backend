package com.lovemap.lovemapbackend.utils

import com.fasterxml.jackson.databind.ObjectMapper

val objectMapper = ObjectMapper()

data class ErrorMessages(
    val errors: List<ErrorMessage>
) {
    fun toJson(): String {
        return objectMapper.writeValueAsString(this)
    }
}

data class ErrorMessage(
    val errorCode: ErrorCode,
    val subject: String,
    val message: String,
) {
    fun toJson(): String {
        return objectMapper.writeValueAsString(this)
    }
}

enum class ErrorCode {
    UserOccupied,
    EmailOccupied,
    InvalidCredentials,
    InvalidCredentialsEmail,
    InvalidCredentialsUser,
    InvalidCredentialsPassword,
    NotFoundByLink,
    NotFoundById,
    ConstraintViolation,
    Forbidden,
    Conflict,
    BadRequest,
    YouBlockedHimUnblockFirst,
    BlockedByUser,
    RelationNotFound,
    PartnershipNotFound,
    AlreadyPartners,
    PartnershipRerequestTimeNotPassed,
    PartnershipAlreadyRequested,
    InvalidOperationOnYourself,
}