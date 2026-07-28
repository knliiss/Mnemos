package com.mnemos.accountservice.domain.exception.account

import com.mnemos.common.core.model.user.UserId
import com.mnemos.common.web.exception.AppException
import org.springframework.http.HttpStatus

class ActiveAccountNotSetException(
    userId: UserId,
) : AppException(
        message = "User doesn't have active account",
        code = "ACTIVE_ACCOUNT_NOT_SET",
        httpStatus = HttpStatus.BAD_REQUEST,
        details = mapOf("userId" to userId),
    )
