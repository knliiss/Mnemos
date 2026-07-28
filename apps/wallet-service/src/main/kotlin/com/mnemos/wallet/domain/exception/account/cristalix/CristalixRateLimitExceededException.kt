package com.mnemos.accountservice.domain.exception.account.cristalix

import com.mnemos.common.web.exception.AppException
import org.springframework.http.HttpStatus

class CristalixRateLimitExceededException :
    AppException(
        message = "Cristalix API is temporarily overloaded",
        code = "CRISTALIX_RATE_LIMIT_EXCEEDED",
        httpStatus = HttpStatus.SERVICE_UNAVAILABLE,
        details = emptyMap(),
    )
