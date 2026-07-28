package com.mnemos.accountservice.domain.exception.account.cristalix

import com.mnemos.common.web.exception.AppException
import org.springframework.http.HttpStatus

class CristalixUnavailableException :
    AppException(
        message = "Cristalix API is temporarily unavailable",
        code = "CRISTALIX_UNAVAILABLE",
        httpStatus = HttpStatus.SERVICE_UNAVAILABLE,
        details = emptyMap(),
    )
