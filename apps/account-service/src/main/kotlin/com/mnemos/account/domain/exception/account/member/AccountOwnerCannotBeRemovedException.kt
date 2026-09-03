package com.mnemos.account.domain.exception.account.member

import com.mnemos.common.core.model.id.AccountId
import com.mnemos.common.web.exception.AppException
import org.springframework.http.HttpStatus

class AccountOwnerCannotBeRemovedException(
    accountId: AccountId,
) : AppException(
        message = "Account owner cannot be removed before ownership is transferred",
        code = "ACCOUNT_OWNER_CANNOT_BE_REMOVED",
        httpStatus = HttpStatus.CONFLICT,
        details = mapOf("accountId" to accountId.toString()),
    )
