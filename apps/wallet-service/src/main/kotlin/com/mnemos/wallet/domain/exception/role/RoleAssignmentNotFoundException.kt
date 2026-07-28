package com.mnemos.accountservice.domain.exception.role

import com.mnemos.accountservice.domain.model.account.AccountMemberId
import com.mnemos.accountservice.domain.model.account.role.RoleId
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
