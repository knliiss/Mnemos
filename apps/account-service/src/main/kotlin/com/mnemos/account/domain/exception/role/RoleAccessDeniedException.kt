package com.mnemos.account.domain.exception.role

import com.mnemos.account.domain.exception.role.RoleAccessDeniedReason
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
                "accountId" to accountId.id,
                "reason" to reason.name,
            ),
    )
