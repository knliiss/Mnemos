package com.mnemos.common.web.exception

import org.springframework.http.HttpStatus

open class AppException(
    message: String,
    val code: String,
    val httpStatus: HttpStatus,
    val details: Map<String, Any> = emptyMap(),
    cause: Throwable? = null,
) : RuntimeException(message, cause)