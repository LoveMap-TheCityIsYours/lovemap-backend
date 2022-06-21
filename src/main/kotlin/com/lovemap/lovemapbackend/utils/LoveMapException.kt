package com.lovemap.lovemapbackend.utils

import org.springframework.http.HttpStatus

class LoveMapException(val status: HttpStatus, val errorMessages: ErrorMessages) :
    RuntimeException(errorMessages.toJson()) {

    constructor(status: HttpStatus, errorMessage: ErrorMessage)
            : this(status, ErrorMessages(listOf(errorMessage)))
}