package com.mnemos.moderation.application.security.exception

import com.mnemos.common.web.exception.AppException
import org.springframework.http.HttpStatus

class UserRoleLookupUnavailableException(
    cause: Throwable,
) : AppException(
        message = "Unable to resolve the target user's moderation role",
        code = "USER_ROLE_LOOKUP_UNAVAILABLE",
        httpStatus = HttpStatus.SERVICE_UNAVAILABLE,
        cause = cause,
    )
