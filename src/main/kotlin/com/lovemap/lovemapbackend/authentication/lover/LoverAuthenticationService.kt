package com.lovemap.lovemapbackend.authentication.lover

import com.lovemap.lovemapbackend.geolocation.GeoLocation
import com.lovemap.lovemapbackend.lover.Lover
import com.lovemap.lovemapbackend.utils.ErrorCode
import com.lovemap.lovemapbackend.utils.ErrorMessage
import com.lovemap.lovemapbackend.utils.LoveMapException
import org.springframework.http.HttpStatus
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.sql.Timestamp
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.*

@Service
@Transactional
class LoverAuthenticationService(
    private val passwordEncoder: PasswordEncoder,
    private val repository: LoverAuthenticationRepository
) {
    suspend fun createPassword(lover: Lover, password: String) {
        save(
            LoverAuthentication(
                passwordHash = passwordEncoder.encode(password),
                loverId = lover.id,
            )
        )
    }

    suspend fun createFacebookAuth(lover: Lover, facebookId: String) {
        save(
            LoverAuthentication(
                passwordHash = null,
                loverId = lover.id,
                passwordSet = false,
                facebookId = facebookId
            )
        )
    }

    suspend fun findByFacebookId(facebookId: String): LoverAuthentication? {
        return repository.findByFacebookId(facebookId)
    }

    suspend fun save(authentication: LoverAuthentication): LoverAuthentication {
        return repository.save(authentication)
    }

    suspend fun getLoverAuthentication(lover: Lover): LoverAuthentication {
        return repository.findByLoverId(lover.id) ?: throwForbidden(lover)
    }

    suspend fun initPasswordReset(lover: Lover): String {
        val password = getLoverAuthentication(lover)
        checkResetBackoffPassed(password, lover)
        password.resetCode = UUID.randomUUID().toString().substringBefore("-").uppercase()
        password.resetInitiatedAt = Timestamp.from(Instant.now())
        repository.save(password)
        return password.resetCode!!
    }

    suspend fun setNewPassword(lover: Lover, resetCode: String, newPassword: String) {
        val password = getLoverAuthentication(lover)
        checkResetCode(password, lover, resetCode)
        password.resetCode = null
        password.resetInitiatedAt = null
        password.passwordHash = passwordEncoder.encode(newPassword)
        save(password)
    }

    private fun checkResetBackoffPassed(
        password: LoverAuthentication,
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
        password: LoverAuthentication,
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

    private fun checkResetCodeStillValid(password: LoverAuthentication, lover: Lover) {
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

    suspend fun getDisplayName(userName: String, email: String): String {
        return if (userName == email) {
            email.substringBefore("@")
        } else {
            userName
        }
    }

    suspend fun getRegistrationCountry(registrationCountry: String?): String =
        registrationCountry?.ifEmpty { GeoLocation.GLOBAL_LOCATION } ?: GeoLocation.GLOBAL_LOCATION
}