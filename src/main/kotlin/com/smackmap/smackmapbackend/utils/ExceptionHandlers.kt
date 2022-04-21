package com.smackmap.smackmapbackend.utils

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.server.ResponseStatusException
import javax.validation.ConstraintViolationException

@RestControllerAdvice
class ExceptionHandlers {
    @ExceptionHandler(ConstraintViolationException::class)
    fun constraintViolationExcetion(ex: ConstraintViolationException): ResponseEntity<String> {
        return ResponseEntity.badRequest().body(ex.message)
    }

    @ExceptionHandler(ResponseStatusException::class)
    fun responseStatusException(ex: ResponseStatusException): ResponseEntity<String> {
        return ResponseEntity.status(ex.status).body(ex.reason)
    }
}