package com.mnemos.event.domain.wipe.exception

import com.mnemos.common.web.exception.AppException
import org.springframework.http.HttpStatus

class WipeValidationException(
    message: String,
) : AppException(
        message = message,
        code = "INVALID_WIPE_BOUNDARY",
        httpStatus = HttpStatus.BAD_REQUEST,
    )
