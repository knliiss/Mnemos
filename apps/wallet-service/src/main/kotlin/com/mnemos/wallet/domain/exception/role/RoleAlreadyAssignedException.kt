package com.mnemos.accountservice.domain.exception.role

import com.mnemos.accountservice.domain.model.account.AccountMemberId
import com.mnemos.accountservice.domain.model.account.role.RoleId
import com.mnemos.common.web.exception.AppException
import org.springframework.http.HttpStatus

class RoleAlreadyAssignedException(
    accountMemberId: AccountMemberId,
    roleId: RoleId,
) : AppException(
        message = "Role is already assigned to account member",
        code = "ROLE_ALREADY_ASSIGNED",
        httpStatus = HttpStatus.CONFLICT,
        details =
            mapOf(
                "accountMemberId" to accountMemberId.value,
                "roleId" to roleId.value,
            ),
    )
