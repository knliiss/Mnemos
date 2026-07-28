package com.mnemos.account.domain.exception.account.member

import com.mnemos.account.domain.model.account.AccountMemberId
import com.mnemos.common.core.model.id.AccountId
import com.mnemos.common.core.model.user.UserId
import com.mnemos.common.web.exception.AppException
import org.springframework.http.HttpStatus

class AccountMemberNotFoundException(
    memberId: AccountMemberId? = null,
    userId: UserId? = null,
    accountId: AccountId? = null,
) : AppException(
        message = "Account member not found",
        code = "ACCOUNT_MEMBER_NOT_FOUND",
        httpStatus = HttpStatus.NOT_FOUND,
        details =
            buildMap {
                if (memberId != null) put("member_id", memberId)

                if (userId != null) put("userId", userId.value)
                if (accountId != null) put("accountId", accountId.value)
            },
    )
