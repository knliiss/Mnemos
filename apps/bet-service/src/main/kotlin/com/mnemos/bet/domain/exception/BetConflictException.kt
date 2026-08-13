package com.mnemos.bet.domain.exception

import com.mnemos.common.web.exception.AppException
import org.springframework.http.HttpStatus

class BetConflictException(
    message: String,
) : AppException(
        message = message,
        code = "BET_CONFLICT",
        httpStatus = HttpStatus.CONFLICT,
    )
