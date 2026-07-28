package com.mnemos.account.domain.exception.role

import com.mnemos.common.core.model.id.AccountId
import com.mnemos.common.web.exception.AppException
import org.springframework.http.HttpStatus

class InvalidRoleHierarchyException(
    accountId: AccountId,
    reason: String,
) : AppException(
        message = "Invalid account role hierarchy",
        code = "INVALID_ROLE_HIERARCHY",
        httpStatus = HttpStatus.BAD_REQUEST,
        details =
            mapOf(
                "accountId" to accountId.value,
                "reason" to reason,
            ),
    )
