package com.mnemos.common.web.exception.common

import com.mnemos.common.core.model.id.AccountId
import com.mnemos.common.web.exception.AppException
import org.springframework.http.HttpStatus

class AccountPermissionDeniedException(
    accountId: AccountId,
    requiredPermissions: Set<String> = emptySet(),
) : AppException(
    message = "Account permission denied",
    code = "ACCOUNT_PERMISSION_DENIED",
    httpStatus = HttpStatus.FORBIDDEN,
    details =
        mapOf(
            "accountId" to accountId.id,
            "neededPermissions" to requiredPermissions,
        ),
)
