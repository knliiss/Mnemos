package com.mnemos.accountservice.domain.exception.account.cristalix

import com.mnemos.common.web.exception.AppException
import org.springframework.http.HttpStatus

class CristalixAuthorizationException :
    AppException(
        message = "Cristalix API authorization failed",
        code = "CRISTALIX_AUTHORIZATION_FAILED",
        httpStatus = HttpStatus.BAD_GATEWAY,
        details = emptyMap(),
    )
