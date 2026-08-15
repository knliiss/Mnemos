package com.mnemos.personalization.application.account.exception

import com.mnemos.common.web.exception.AppException
import org.springframework.http.HttpStatus

class ActiveAccountRequiredException :
    AppException(
        "Active account is required",
        "ACTIVE_ACCOUNT_REQUIRED",
        HttpStatus.BAD_REQUEST,
    )
