package com.mnemos.account.domain.exception.role

import com.mnemos.account.domain.model.account.AccountMemberId
import com.mnemos.common.web.exception.AppException
import org.springframework.http.HttpStatus

class RoleHierarchyInconsistentException(
    accountMemberId: AccountMemberId,
) : AppException(
        message = "Account member has role management permission but has no assigned role",
        code = "ROLE_HIERARCHY_INCONSISTENT",
        httpStatus = HttpStatus.INTERNAL_SERVER_ERROR,
        details =
            mapOf(
                "accountMemberId" to accountMemberId.id,
            ),
    )
