package com.mnemos.security.exception

import com.mnemos.common.web.exception.AppException
import org.springframework.http.HttpStatus

class InvalidAuthenticationPrincipalException(
    message: String,
    details: Map<String, Any> = emptyMap(),
    cause: Throwable? = null,
) : AppException(
    message = message,
    code = "INVALID_AUTHENTICATION_PRINCIPAL",
    httpStatus = HttpStatus.UNAUTHORIZED,
    details = details,
    cause = cause
) {
}