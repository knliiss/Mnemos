package com.mnemos.bet.domain.exception

import com.mnemos.common.web.exception.AppException
import org.springframework.http.HttpStatus

class BetNotFoundException(
    message: String,
) : AppException(
        message = message,
        code = "BET_NOT_FOUND",
        httpStatus = HttpStatus.NOT_FOUND,
    )
