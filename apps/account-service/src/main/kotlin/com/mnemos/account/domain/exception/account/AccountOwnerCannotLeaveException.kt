package com.mnemos.account.domain.exception.account

import com.mnemos.common.core.model.id.AccountId
import com.mnemos.common.web.exception.AppException
import org.springframework.http.HttpStatus

class AccountOwnerCannotLeaveException(
    val accountId: AccountId,
) : AppException(
        message = "Owner cannot leave the account",
        code = "ACCOUNT_OWNER_CANNOT_LEAVE_ACCOUNT",
        httpStatus = HttpStatus.CONFLICT,
        details = mapOf("accountId" to accountId.value),
    )
