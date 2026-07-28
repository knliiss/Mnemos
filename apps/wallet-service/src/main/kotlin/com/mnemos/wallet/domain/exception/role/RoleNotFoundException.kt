package com.mnemos.accountservice.domain.exception.role

import com.mnemos.accountservice.domain.model.account.role.RoleId
import com.mnemos.common.core.model.id.AccountId
import com.mnemos.common.web.exception.AppException
import org.springframework.http.HttpStatus

class RoleNotFoundException(
    roleId: RoleId,
    accountId: AccountId,
) : AppException(
        message = "Role not found",
        code = "ROLE_NOT_FOUND",
        httpStatus = HttpStatus.NOT_FOUND,
        details = mapOf("roleId" to roleId.value, "accountId" to accountId.value),
    )
