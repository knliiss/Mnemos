package com.mnemos.account.domain.exception.user

import com.mnemos.common.core.model.id.KeycloakId
import com.mnemos.common.web.exception.AppException
import org.springframework.http.HttpStatus

class UserNotFoundException(
    keycloakId: KeycloakId,
) : AppException(
        message = "User not found",
        code = "USER_NOT_FOUND",
        httpStatus = HttpStatus.NOT_FOUND,
        details = mapOf("keycloakId" to keycloakId.id),
    )
