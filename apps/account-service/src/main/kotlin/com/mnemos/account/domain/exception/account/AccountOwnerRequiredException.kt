package com.mnemos.account.domain.exception.account

import com.mnemos.common.core.model.id.AccountId
import com.mnemos.common.web.exception.AppException
import org.springframework.http.HttpStatus

class AccountOwnerRequiredException(
    accountId: AccountId,
) : AppException(
        message = "Only the account owner can transfer ownership",
        code = "ACCOUNT_OWNER_REQUIRED",
        httpStatus = HttpStatus.FORBIDDEN,
        details =
            mapOf(
                "accountId" to accountId.value,
            ),
    )
