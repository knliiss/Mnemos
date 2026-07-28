package com.mnemos.accountservice.domain.exception.account

import com.mnemos.common.core.model.id.AccountId
import com.mnemos.common.web.exception.AppException
import org.springframework.http.HttpStatus

class AccountNotFoundException(
    accountId: AccountId,
) : AppException(
        message = "Account not found",
        code = "ACCOUNT_NOT_FOUND",
        httpStatus = HttpStatus.NOT_FOUND,
        details = mapOf("accountId" to accountId.value),
    )
