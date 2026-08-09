package com.mnemos.account.domain.exception.account

import com.mnemos.common.core.model.id.AccountId
import com.mnemos.common.web.exception.AppException
import org.springframework.http.HttpStatus

class AccountNotActiveException(
    id: AccountId,
) : AppException(
        message = "Account is not active",
        code = "ACCOUNT_NOT_ACTIVE",
        httpStatus = HttpStatus.FORBIDDEN,
        details = mapOf("accountId" to id.id),
    )
