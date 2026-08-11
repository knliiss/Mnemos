package com.mnemos.reward.domain.daily.exception

import com.mnemos.common.web.exception.AppException
import org.springframework.http.HttpStatus

class ActiveAccountRequiredException :
    AppException(
        "Active account is required",
        "ACTIVE_ACCOUNT_REQUIRED",
        HttpStatus.BAD_REQUEST,
    )
