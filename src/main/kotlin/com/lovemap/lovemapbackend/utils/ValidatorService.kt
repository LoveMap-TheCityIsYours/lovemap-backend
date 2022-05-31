package com.lovemap.lovemapbackend.utils

import com.lovemap.lovemapbackend.utils.ErrorCode.ConstraintViolation
import org.springframework.stereotype.Service
import javax.validation.ConstraintViolationException
import javax.validation.Validator

const val INVALID_EMAIL = "Invalid email address"
const val INVALID_USERNAME = "Length of username must be between 3 and 25 characters."
const val INVALID_PASSWORD = "Length of password must be between 6 and 100 characters."
const val INVALID_LOVE_SPOT_NAME = "Name must be between 3 and 50 characters."
const val INVALID_LOVE_DESCRIPTION = "Description must be between 5 and 250 characters."

private val constraintMap = HashMap<String, ErrorCode>().apply {
    put(INVALID_EMAIL, ErrorCode.InvalidCredentialsEmail)
    put(INVALID_USERNAME, ErrorCode.InvalidCredentialsUser)
    put(INVALID_PASSWORD, ErrorCode.InvalidCredentialsPassword)
    put(INVALID_LOVE_SPOT_NAME, ErrorCode.InvalidLoveSpotName)
    put(INVALID_LOVE_DESCRIPTION, ErrorCode.InvalidLoveSpotDescription)
}

@Service
class ValidatorService(
    private val validator: Validator
) {
    fun validate(request: Any) {
        val violations = validator.validate(request)
        if (violations.isNotEmpty()) {
            val errorMessages = ErrorMessages(violations.map {
                val errorCode: ErrorCode = constraintMap[it.message] ?: ConstraintViolation
                ErrorMessage(errorCode, it.invalidValue.toString(), it.message)
            }.toList())
            throw ConstraintViolationException(errorMessages.toJson(), violations)
        }
    }
}