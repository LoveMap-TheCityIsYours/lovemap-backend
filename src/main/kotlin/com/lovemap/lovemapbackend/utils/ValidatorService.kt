package com.lovemap.lovemapbackend.utils

import com.lovemap.lovemapbackend.utils.ErrorCode.ConstraintViolation
import org.springframework.stereotype.Service
import javax.validation.ConstraintViolationException
import javax.validation.Validator

@Service
class ValidatorService(
    private val validator: Validator
) {
    fun validate(request: Any) {
        val violations = validator.validate(request)
        if (violations.isNotEmpty()) {
            val errorMessages = ErrorMessages(violations.map {
                ErrorMessage(ConstraintViolation, it.invalidValue.toString(), it.message)
            }.toList())
            throw ConstraintViolationException(errorMessages.toJson(), violations)
        }
    }
}