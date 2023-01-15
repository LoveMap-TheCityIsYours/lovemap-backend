package com.lovemap.lovemapbackend.utils

import com.lovemap.lovemapbackend.lovespot.query.MAX_LIMIT_LIST
import com.lovemap.lovemapbackend.lovespot.query.MAX_LIMIT_SEARCH
import com.lovemap.lovemapbackend.utils.ErrorCode.ConstraintViolation
import jakarta.validation.Validator
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service

const val INVALID_EMAIL = "Invalid email address"
const val INVALID_USERNAME = "Length of name must be between 3 and 32 characters."
const val INVALID_PASSWORD = "Length of password must be between 6 and 100 characters."
const val INVALID_LOVE_SPOT_NAME = "Name must be between 3 and 50 characters."
const val INVALID_LOVE_DESCRIPTION = "Description must be between 5 and 1000 characters."
const val INVALID_PW_RESET_CODE = "Password reset code is exactly 8 characters long."
const val INVALID_DISTANCE_IN_METERS = "Allowed 'distance in meters' value is between 1 and 1 000 000"
const val INVALID_LIST_LIMIT = "Allowed 'limit' value is between 0 and $MAX_LIMIT_LIST"
const val INVALID_SEARCH_LIMIT = "Allowed 'limit' value is between 0 and $MAX_LIMIT_SEARCH"

private val constraintMap = HashMap<String, ErrorCode>().apply {
    put(INVALID_EMAIL, ErrorCode.InvalidCredentialsEmail)
    put(INVALID_USERNAME, ErrorCode.InvalidCredentialsUser)
    put(INVALID_PASSWORD, ErrorCode.InvalidCredentialsPassword)
    put(INVALID_LOVE_SPOT_NAME, ErrorCode.InvalidLoveSpotName)
    put(INVALID_LOVE_DESCRIPTION, ErrorCode.InvalidLoveSpotDescription)
    put(INVALID_PW_RESET_CODE, ErrorCode.InvalidPwResetCode)
    put(INVALID_DISTANCE_IN_METERS, ErrorCode.InvalidDistanceInMeters)
    put(INVALID_SEARCH_LIMIT, ErrorCode.InvalidLimit)
    put(INVALID_LIST_LIMIT, ErrorCode.InvalidLimit)
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

    fun validatePageRequest(page: Int, size: Int, totalCount: Int) {
        if (page * size > totalCount) {
            throw LoveMapException(HttpStatus.BAD_REQUEST, ErrorCode.NewsFeedPageNotFound)
        }
        if (page < 0 || size < 1) {
            throw LoveMapException(HttpStatus.BAD_REQUEST, ErrorCode.InvalidPageRequest)
        }
    }
}
