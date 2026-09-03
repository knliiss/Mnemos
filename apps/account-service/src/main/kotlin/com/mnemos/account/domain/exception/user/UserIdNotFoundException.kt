package com.mnemos.account.domain.exception.user

import com.mnemos.common.core.model.id.UserId
import com.mnemos.common.web.exception.AppException
import org.springframework.http.HttpStatus

class UserIdNotFoundException(
    userId: UserId,
) : AppException(
        message = "User not found",
        code = "USER_NOT_FOUND",
        httpStatus = HttpStatus.NOT_FOUND,
        details = mapOf("userId" to userId.toString()),
    )
