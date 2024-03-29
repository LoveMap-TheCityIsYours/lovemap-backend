package com.lovemap.lovemapbackend.utils

import com.fasterxml.jackson.databind.ObjectMapper
import jakarta.validation.ConstraintViolationException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.server.ResponseStatusException

@RestControllerAdvice
class ExceptionHandlers {
    private val objectMapper = ObjectMapper()

    // TODO: make this return proper ErrorMessages type always
    @ExceptionHandler(ConstraintViolationException::class)
    fun constraintViolationExcetion(ex: ConstraintViolationException): ResponseEntity<String> {
        return ResponseEntity.badRequest().body(ex.message)
    }

    @ExceptionHandler(ResponseStatusException::class)
    fun responseStatusException(ex: ResponseStatusException): ResponseEntity<String> {
        if (ex.statusCode == HttpStatus.FORBIDDEN) {
            return ResponseEntity.status(ex.statusCode).body(
                ErrorMessages(listOf(ErrorMessage(ErrorCode.InvalidCredentials))).toJson()
            )
        }
        return ResponseEntity.status(ex.statusCode).body(ex.reason)
    }

    @ExceptionHandler(LoveMapException::class)
    fun loveMapExceptionHandler(ex: LoveMapException): ResponseEntity<ErrorMessages> {
        return ResponseEntity.status(ex.status).body(ex.errorMessages)
    }
}