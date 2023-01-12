package com.lovemap.lovemapbackend.authentication

import com.lovemap.lovemapbackend.lover.LoverResponse
import com.lovemap.lovemapbackend.utils.*
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotEmpty
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size

data class CreateLoverRequest(
    @field:Size(min = 3, max = 25, message = INVALID_USERNAME)
    val userName: String,
    @field:Size(min = 6, max = 100, message = INVALID_PASSWORD)
    val password: String,
    @field:NotEmpty(message = INVALID_EMAIL)
    @field:Email(message = INVALID_EMAIL)
    val email: String,
    val registrationCountry: String?
)

data class LoginLoverRequest(
    @field:Size(min = 3, max = 25, message = INVALID_USERNAME)
    val userName: String? = null,
    @field:Email(message = INVALID_EMAIL)
    val email: String?,
    @field:Size(min = 6, max = 100, message = INVALID_PASSWORD)
    val password: String
)

data class ResetPasswordRequest(
    @field:NotEmpty(message = INVALID_EMAIL)
    @field:Email(message = INVALID_EMAIL)
    val email: String
)

data class ResetPasswordResponse(
    val text: String
)

data class NewPasswordRequest(
    @field:NotEmpty(message = INVALID_EMAIL)
    @field:Email(message = INVALID_EMAIL)
    val email: String,
    @field:Size(min = 8, max = 8, message = INVALID_PW_RESET_CODE)
    val resetCode: String,
    @field:Size(min = 6, max = 100, message = INVALID_PASSWORD)
    val newPassword: String
)

data class FacebookAuthenticationRequest(
    @field:NotEmpty(message = INVALID_EMAIL)
    @field:Email(message = INVALID_EMAIL)
    val email: String,
    @field:NotNull
    val facebookId: String,
    @field:NotNull
    val accessToken: String,
    val registrationCountry: String?
)

data class LoverAuthenticationResult(
    val loverResponse: LoverResponse,
    val jwt: String
)






