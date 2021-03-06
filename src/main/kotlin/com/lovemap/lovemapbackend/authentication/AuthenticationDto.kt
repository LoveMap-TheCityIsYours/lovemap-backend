package com.lovemap.lovemapbackend.authentication

import com.lovemap.lovemapbackend.utils.INVALID_EMAIL
import com.lovemap.lovemapbackend.utils.INVALID_PASSWORD
import com.lovemap.lovemapbackend.utils.INVALID_PW_RESET_CODE
import com.lovemap.lovemapbackend.utils.INVALID_USERNAME
import javax.validation.constraints.Email
import javax.validation.constraints.Size

data class CreateLoverRequest(
    @field:Size(min = 3, max = 25, message = INVALID_USERNAME)
    val userName: String,
    @field:Size(min = 6, max = 100, message = INVALID_PASSWORD)
    val password: String,
    @field:Email(message = INVALID_EMAIL)
    val email: String
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
    @field:Email(message = INVALID_EMAIL)
    val email: String
)

data class ResetPasswordResponse(
    val text: String
)

data class NewPasswordRequest(
    @field:Email(message = INVALID_EMAIL)
    val email: String,
    @field:Size(min = 8, max = 8, message = INVALID_PW_RESET_CODE)
    val resetCode: String,
    @field:Size(min = 6, max = 100, message = INVALID_PASSWORD)
    val newPassword: String
)