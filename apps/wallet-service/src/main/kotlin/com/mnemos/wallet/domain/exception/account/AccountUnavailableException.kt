package com.mnemos.accountservice.domain.exception.account

import com.mnemos.accountservice.domain.model.account.AccountStatus
import com.mnemos.common.core.model.id.AccountId
import com.mnemos.common.web.exception.AppException
import org.springframework.http.HttpStatus

class AccountUnavailableException(
    accountId: AccountId,
    status: AccountStatus,
) : AppException(
        message = "Account is unavailable",
        code = "ACCOUNT_UNAVAILABLE",
        httpStatus = HttpStatus.FORBIDDEN,
        details =
            mapOf(
                "accountId" to accountId.value,
                "status" to status.name,
            ),
    )
