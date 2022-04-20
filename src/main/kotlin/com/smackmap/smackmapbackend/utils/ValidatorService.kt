package com.smackmap.smackmapbackend.utils

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
            throw ConstraintViolationException(violations)
        }
    }
}