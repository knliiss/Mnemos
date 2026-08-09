package com.mnemos.account.domain.exception.account

import com.mnemos.common.core.model.id.AccountId
import com.mnemos.common.web.exception.AppException
import org.springframework.http.HttpStatus

class AccountAccessDeniedException(
    accountId: AccountId,
) : AppException(
        message = "You have not allowed to access the account",
        code = "ACCOUNT_ACCESS_DENIED",
        httpStatus = HttpStatus.FORBIDDEN,
        details = mapOf("accountId" to accountId.id),
    )
