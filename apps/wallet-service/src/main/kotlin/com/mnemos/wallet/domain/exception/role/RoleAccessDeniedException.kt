package com.mnemos.accountservice.domain.exception.account.role

import com.mnemos.accountservice.domain.exception.role.RoleAccessDeniedReason
import com.mnemos.common.core.model.id.AccountId
import com.mnemos.common.web.exception.AppException
import org.springframework.http.HttpStatus

class RoleAccessDeniedException(
    accountId: AccountId,
    reason: RoleAccessDeniedReason,
) : AppException(
        message = "Role operation is not allowed by the role hierarchy",
        code = "ROLE_ACCESS_DENIED",
        httpStatus = HttpStatus.FORBIDDEN,
        details =
            mapOf(
                "accountId" to accountId.value,
                "reason" to reason.name,
            ),
    )
