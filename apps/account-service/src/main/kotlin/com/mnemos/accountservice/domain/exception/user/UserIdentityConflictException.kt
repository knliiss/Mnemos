package com.mnemos.accountservice.domain.exception

import com.mnemos.common.web.exception.AppException
import org.springframework.http.HttpStatus

class UserIdentityConflictException(
    message: String,
    details: Map<String, Any> = emptyMap(),
) : AppException(
    message = message,
    code = "USER_IDENTITY_CONFLICT",
    httpStatus = HttpStatus.CONFLICT,
    details = details
)