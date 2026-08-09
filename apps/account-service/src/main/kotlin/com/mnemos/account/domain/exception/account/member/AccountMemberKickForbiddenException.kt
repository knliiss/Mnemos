package com.mnemos.account.domain.exception.account.member

import com.mnemos.account.domain.model.account.AccountMemberId
import com.mnemos.common.web.exception.AppException
import org.springframework.http.HttpStatus

class AccountMemberKickForbiddenException(
    actorMemberId: AccountMemberId,
    targetMemberId: AccountMemberId,
) : AppException(
        message = "Account member cannot be kicked",
        code = "ACCOUNT_MEMBER_KICK_FORBIDDEN",
        httpStatus = HttpStatus.FORBIDDEN,
        details =
            mapOf(
                "actorMemberId" to actorMemberId.id,
                "targetMemberId" to targetMemberId.id,
            ),
    )
