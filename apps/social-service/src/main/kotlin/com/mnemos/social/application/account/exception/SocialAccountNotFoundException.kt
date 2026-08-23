package com.mnemos.social.application.account.exception

import com.mnemos.common.web.exception.AppException
import org.springframework.http.HttpStatus
import java.util.UUID

class SocialAccountNotFoundException(
    accountId: UUID,
) : AppException(
        message = "Target account was not found",
        code = "SOCIAL_ACCOUNT_NOT_FOUND",
        httpStatus = HttpStatus.NOT_FOUND,
        details = mapOf("accountId" to accountId),
    )
