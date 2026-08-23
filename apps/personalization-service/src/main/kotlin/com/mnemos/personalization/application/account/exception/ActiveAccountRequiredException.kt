package com.mnemos.personalization.application.account.exception

import com.mnemos.common.web.exception.AppException
import org.springframework.http.HttpStatus

class ActiveAccountRequiredException :
    AppException(
        message = "Active account is required",
        code = "ACTIVE_ACCOUNT_REQUIRED",
        httpStatus = HttpStatus.BAD_REQUEST,
    )
