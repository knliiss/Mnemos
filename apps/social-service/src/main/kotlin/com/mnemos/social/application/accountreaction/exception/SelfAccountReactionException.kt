package com.mnemos.social.application.accountreaction.exception

import com.mnemos.common.web.exception.AppException
import org.springframework.http.HttpStatus
import java.util.UUID

class SelfAccountReactionException(
    accountId: UUID,
) : AppException(
        message = "An account cannot react to itself",
        code = "SELF_ACCOUNT_REACTION_NOT_ALLOWED",
        httpStatus = HttpStatus.BAD_REQUEST,
        details = mapOf("accountId" to accountId),
    )
