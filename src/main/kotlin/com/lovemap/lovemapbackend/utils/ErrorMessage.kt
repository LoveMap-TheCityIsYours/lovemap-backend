package com.lovemap.lovemapbackend.utils

import com.fasterxml.jackson.databind.ObjectMapper

val objectMapper = ObjectMapper()

data class ErrorMessages(
    val errors: List<ErrorMessage>
) {
    fun toJson(): String {
        return objectMapper.writeValueAsString(this)
    }

    override fun toString(): String {
        return toJson()
    }
}

data class ErrorMessage(
    val errorCode: ErrorCode,
    val subject: String,
    val message: String,
) {
    constructor(errorCode: ErrorCode) : this(errorCode, "", "")

    fun toJson(): String {
        return objectMapper.writeValueAsString(this)
    }

    override fun toString(): String {
        return toJson()
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
    SpotTooCloseToAnother,
    InvalidLoveSpotName,
    InvalidLoveSpotDescription,
    InvalidPwResetCode,
    PwResetBackoffNotPassed,
    WrongPwResetCode,
    PwResetCodeTimedOut,
    MissingListCountry,
    MissingListCity,
    MissingListCoordinates,
    InvalidDistanceInMeters,
    InvalidLimit,
    InvalidListLocationType,
    FacebookEmailOccupied,
    FacebookLoginFailed,
    UnsupportedImageFormat,
    ImageUploadFailed,
    PhotoNotFound,
    WishlistItemNotFound,
    AlreadyOnWishlist,
    LoveSpotNotFound,

}