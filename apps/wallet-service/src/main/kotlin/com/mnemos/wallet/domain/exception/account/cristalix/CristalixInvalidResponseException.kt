package com.mnemos.accountservice.domain.exception.account.cristalix

import com.mnemos.common.web.exception.AppException
import org.springframework.http.HttpStatus

class CristalixInvalidResponseException :
    AppException(
        message = "Cristalix API returned an invalid response",
        code = "CRISTALIX_INVALID_RESPONSE",
        httpStatus = HttpStatus.BAD_GATEWAY,
        details = emptyMap(),
    )
