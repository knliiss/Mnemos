package com.mnemos.social.application.actor.exception

import com.mnemos.common.web.exception.AppException
import org.springframework.http.HttpStatus

class ActiveSocialAccountRequiredException :
    AppException(
        message = "Active account is required",
        code = "ACTIVE_ACCOUNT_REQUIRED",
        httpStatus = HttpStatus.BAD_REQUEST,
    )
