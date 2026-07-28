package com.mnemos.account.domain.exception.role

import com.mnemos.account.domain.model.account.AccountMemberId
import com.mnemos.account.domain.model.account.role.RoleId
import com.mnemos.common.web.exception.AppException
import org.springframework.http.HttpStatus

class RoleHierarchyViolationException(
    actorMemberId: AccountMemberId,
    roleId: RoleId? = null,
    subjectMemberId: AccountMemberId? = null,
) : AppException(
        message = "Role cannot be managed because of the role hierarchy",
        code = "ROLE_HIERARCHY_VIOLATION",
        httpStatus = HttpStatus.FORBIDDEN,
        details =
            buildMap {
                put("actorMemberId", actorMemberId.value)

                if (roleId != null) put("roleId", roleId.value)
                if (subjectMemberId != null) put("subjectMemberId", subjectMemberId.value)
            },
    )
