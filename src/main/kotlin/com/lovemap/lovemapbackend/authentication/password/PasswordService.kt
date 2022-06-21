package com.lovemap.lovemapbackend.authentication.password

import com.lovemap.lovemapbackend.lover.Lover
import com.lovemap.lovemapbackend.utils.ErrorCode
import com.lovemap.lovemapbackend.utils.ErrorMessage
import com.lovemap.lovemapbackend.utils.LoveMapException
import org.springframework.http.HttpStatus
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.server.ResponseStatusException
import java.sql.Timestamp
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.*

@Service
@Transactional
class PasswordService(
    private val passwordEncoder: PasswordEncoder,
    private val repository: PasswordRepository
) {
    suspend fun createPassword(lover: Lover, password: String) {
        save(
            Password(
                passwordHash = passwordEncoder.encode(password),
                loverId = lover.id,
            )
        )
    }

    suspend fun save(password: Password): Password {
        return repository.save(password)
    }

    suspend fun getPasswordOfLover(lover: Lover): Password {
        return repository.findByLoverId(lover.id) ?: throwForbidden(lover)
    }

    suspend fun initPasswordReset(lover: Lover): String {
        val password = getPasswordOfLover(lover)
        checkResetBackoffPassed(password, lover)
        password.resetCode = UUID.randomUUID().toString().substringBefore("-").uppercase()
        password.resetInitiatedAt = Timestamp.from(Instant.now())
        repository.save(password)
        return password.resetCode!!
    }

    suspend fun setNewPassword(lover: Lover, resetCode: String, newPassword: String) {
        val password = getPasswordOfLover(lover)
        checkResetCode(password, lover, resetCode)
        password.resetCode = null
        password.resetInitiatedAt = null
        password.passwordHash = passwordEncoder.encode(newPassword)
        save(password)
    }

    private fun checkResetBackoffPassed(
        password: Password,
        lover: Lover
    ) {
        password.resetInitiatedAt?.let {
            if (it.toInstant().plus(15, ChronoUnit.MINUTES).isAfter(Instant.now())) {
                throw LoveMapException(
                    HttpStatus.BAD_REQUEST, ErrorMessage(
                        ErrorCode.PwResetBackoffNotPassed,
                        lover.email,
                        "15 minutes should pass between password resets."
                    )
                )
            }
        }
    }

    private fun checkResetCode(
        password: Password,
        lover: Lover,
        resetCode: String
    ) {
        checkResetCodeStillValid(password, lover)
        if (resetCode != password.resetCode) {
            throw LoveMapException(
                HttpStatus.BAD_REQUEST, ErrorMessage(
                    ErrorCode.WrongPwResetCode,
                    resetCode,
                    "The reset code does not match with the one sent in email."
                )
            )
        }
    }

    private fun checkResetCodeStillValid(password: Password, lover: Lover) {
        password.resetInitiatedAt?.let {
            if (it.toInstant().plus(1, ChronoUnit.DAYS).isBefore(Instant.now())) {
                throw LoveMapException(
                    HttpStatus.BAD_REQUEST, ErrorMessage(
                        ErrorCode.PwResetCodeTimedOut,
                        lover.email,
                        "A password reset code is usable for 1 day."
                    )
                )
            }
        }
    }

    private fun <T> throwForbidden(lover: Lover): T {
        throw LoveMapException(
            HttpStatus.FORBIDDEN, ErrorMessage(
                ErrorCode.Forbidden,
                lover.id.toString(),
                "User not found with ID '${lover.id}"
            )
        )
    }
}