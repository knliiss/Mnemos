package com.mnemos.account.domain.exception.account.member

import com.mnemos.common.core.model.id.AccountId
import com.mnemos.common.core.model.user.UserId
import com.mnemos.common.web.exception.AppException
import org.springframework.http.HttpStatus

class AccountMemberAlreadyExistsException(
    accountId: AccountId,
    userId: UserId,
) : AppException(
        message = "User is already a member of this account",
        code = "ACCOUNT_MEMBER_ALREADY_EXISTS",
        httpStatus = HttpStatus.CONFLICT,
        details =
            mapOf(
                "accountId" to accountId.value,
                "userId" to userId.value,
            ),
    )
