package com.mnemos.account.domain.exception.invite

import com.mnemos.common.web.exception.AppException
import org.springframework.http.HttpStatus
import java.time.Instant

class InvalidAccountInviteExpirationException(
    expiresAt: Instant,
) : AppException(
        message = "Account invite expiration must be in the future",
        code = "INVALID_ACCOUNT_INVITE_EXPIRATION",
        httpStatus = HttpStatus.BAD_REQUEST,
        details =
            mapOf(
                "expiresAt" to expiresAt,
            ),
    )
