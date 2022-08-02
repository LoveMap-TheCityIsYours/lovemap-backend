package com.lovemap.lovemapbackend.utils

import com.lovemap.lovemapbackend.lovespot.list.MAX_LIMIT
import com.lovemap.lovemapbackend.utils.ErrorCode.ConstraintViolation
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import javax.validation.Validator

const val INVALID_EMAIL = "Invalid email address"
const val INVALID_USERNAME = "Length of username must be between 3 and 25 characters."
const val INVALID_PASSWORD = "Length of password must be between 6 and 100 characters."
const val INVALID_LOVE_SPOT_NAME = "Name must be between 3 and 50 characters."
const val INVALID_LOVE_DESCRIPTION = "Description must be between 5 and 1000 characters."
const val INVALID_PW_RESET_CODE = "Password reset code is exactly 8 characters long."
const val INVALID_DISTANCE_IN_METERS = "Allowed 'distance in meters' value is between 1 and 1 000 000"
const val INVALID_LIMIT = "Allowed 'limit' value is between 1 and $MAX_LIMIT"

private val constraintMap = HashMap<String, ErrorCode>().apply {
    put(INVALID_EMAIL, ErrorCode.InvalidCredentialsEmail)
    put(INVALID_USERNAME, ErrorCode.InvalidCredentialsUser)
    put(INVALID_PASSWORD, ErrorCode.InvalidCredentialsPassword)
    put(INVALID_LOVE_SPOT_NAME, ErrorCode.InvalidLoveSpotName)
    put(INVALID_LOVE_DESCRIPTION, ErrorCode.InvalidLoveSpotDescription)
    put(INVALID_PW_RESET_CODE, ErrorCode.InvalidPwResetCode)
    put(INVALID_DISTANCE_IN_METERS, ErrorCode.InvalidDistanceInMeters)
    put(INVALID_LIMIT, ErrorCode.InvalidLimit)
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
            throw LoveMapException(HttpStatus.BAD_REQUEST, errorMessages)
        }
    }
}