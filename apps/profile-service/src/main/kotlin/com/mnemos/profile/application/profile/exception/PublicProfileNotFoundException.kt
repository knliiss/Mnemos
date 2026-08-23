package com.mnemos.profile.application.profile.exception

import com.mnemos.common.web.exception.AppException
import org.springframework.http.HttpStatus
import java.util.UUID

class PublicProfileNotFoundException(
    accountId: UUID,
) : AppException(
        message = "Public profile was not found",
        code = "PUBLIC_PROFILE_NOT_FOUND",
        httpStatus = HttpStatus.NOT_FOUND,
        details = mapOf("accountId" to accountId),
    )
