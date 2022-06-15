package com.lovemap.lovemapbackend.authentication.password

import com.lovemap.lovemapbackend.authentication.AuthenticationService
import com.lovemap.lovemapbackend.authentication.LoginLoverRequest
import com.lovemap.lovemapbackend.authentication.NewPasswordRequest
import com.lovemap.lovemapbackend.authentication.ResetPasswordRequest
import com.lovemap.lovemapbackend.email.EmailService
import com.lovemap.lovemapbackend.lover.LoverRelationsDto
import com.lovemap.lovemapbackend.lover.LoverService
import org.springframework.stereotype.Service

@Service
class PasswordResetService(
    private val loverService: LoverService,
    private val passwordService: PasswordService,
    private val authenticationService: AuthenticationService,
    private val emailService: EmailService,
) {
    suspend fun initPasswordReset(request: ResetPasswordRequest) {
        val lover = loverService.unAuthorizedGetByEmail(request.email)
        val resetCode: String = passwordService.initPasswordReset(lover)
        emailService.sendPasswordResetEmail(lover, resetCode)
    }

    suspend fun setNewPassword(request: NewPasswordRequest): LoverRelationsDto {
        val lover = loverService.unAuthorizedGetByEmail(request.email)
        passwordService.setNewPassword(lover, request.resetCode, request.newPassword)
        return authenticationService.loginLover(LoginLoverRequest(email = request.email, password = request.newPassword))
    }
}