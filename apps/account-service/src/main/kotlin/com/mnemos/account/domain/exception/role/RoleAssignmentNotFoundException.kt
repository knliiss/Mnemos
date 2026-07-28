package com.mnemos.account.domain.exception.role

import com.mnemos.account.domain.model.account.AccountMemberId
import com.mnemos.account.domain.model.account.role.RoleId
import com.mnemos.common.web.exception.AppException
import org.springframework.http.HttpStatus

class RoleAssignmentNotFoundException(
    memberId: AccountMemberId,
    roleId: RoleId,
) : AppException(
        message = "Role assignment not found",
        code = "ROLE_ASSIGMENT_NOT_FOUND",
        httpStatus = HttpStatus.NOT_FOUND,
        details = mapOf("roleId" to roleId.toString(), "memberId" to memberId),
    )
