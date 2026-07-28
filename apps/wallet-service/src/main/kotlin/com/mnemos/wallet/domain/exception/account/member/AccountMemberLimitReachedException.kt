package com.mnemos.accountservice.domain.exception.account.member

import com.mnemos.common.core.model.id.AccountId
import com.mnemos.common.web.exception.AppException
import org.springframework.http.HttpStatus

class AccountMemberLimitReachedException(
    accountId: AccountId,
    maxMembers: Int,
) : AppException(
        message = "Account member limit has been reached",
        code = "ACCOUNT_MEMBER_LIMIT_REACHED",
        httpStatus = HttpStatus.CONFLICT,
        details =
            mapOf(
                "accountId" to accountId.value,
                "maxMembers" to maxMembers,
            ),
    )
