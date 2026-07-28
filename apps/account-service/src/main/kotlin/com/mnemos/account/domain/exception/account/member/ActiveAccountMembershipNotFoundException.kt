package com.mnemos.account.domain.exception.account.member

import com.mnemos.common.core.model.id.AccountId
import com.mnemos.common.core.model.user.UserId
import com.mnemos.common.web.exception.AppException
import org.springframework.http.HttpStatus

class ActiveAccountMembershipNotFoundException(
    accountId: AccountId,
    userId: UserId,
) : AppException(
        message = "Active membership was not found",
        code = "ACTIVE_ACCOUNT_MEMBERSHIP_NOT_FOUND",
        httpStatus = HttpStatus.INTERNAL_SERVER_ERROR,
        details = mapOf("accountId" to accountId.value, "userId" to userId.value),
    )
