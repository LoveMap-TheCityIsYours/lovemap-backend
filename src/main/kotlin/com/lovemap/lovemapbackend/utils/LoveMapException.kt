package com.lovemap.lovemapbackend.utils

import org.springframework.http.HttpStatus

class LoveMapException(val status: HttpStatus, val errorMessages: ErrorMessages) :
    RuntimeException(errorMessages.toJson()) {

    constructor(status: HttpStatus, errorMessage: ErrorMessage)
            : this(status, ErrorMessages(listOf(errorMessage)))

    constructor(status: HttpStatus, errorCode: ErrorCode)
            : this(status, ErrorMessages(listOf(ErrorMessage(errorCode, "", ""))))

    constructor(
        status: HttpStatus,
        errorCode: ErrorCode,
        subject: String,
        message: String
    ) : this(status, ErrorMessage(errorCode, subject, message))
}