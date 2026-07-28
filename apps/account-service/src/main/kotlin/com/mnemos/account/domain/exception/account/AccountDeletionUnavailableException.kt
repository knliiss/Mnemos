package com.mnemos.account.domain.exception.account

import com.mnemos.account.domain.model.account.AccountStatus
import com.mnemos.common.core.model.id.AccountId
import com.mnemos.common.web.exception.AppException
import org.springframework.http.HttpStatus

class AccountDeletionUnavailableException(
    accountId: AccountId,
    status: AccountStatus,
) : AppException(
        message = "Account deletion unavailable",
        code = "ACCOUNT_DELETION_UNAVAILABLE",
        httpStatus = HttpStatus.NOT_ACCEPTABLE,
        details = mapOf("accountId" to accountId.toString(), "status" to status.name),
    )
