package com.mnemos.account.domain.exception.account

import com.mnemos.common.core.model.id.AccountId
import com.mnemos.common.web.exception.AppException
import org.springframework.http.HttpStatus

class DeletedAccountImmutableException(
    accountId: AccountId,
) : AppException(
        message = "Deleted account is immutable",
        code = "DELETED_ACCOUNT_IMMUTABLE",
        httpStatus = HttpStatus.CONFLICT,
        details = mapOf("accountId" to accountId.toString()),
    )
