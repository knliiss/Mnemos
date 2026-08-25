package com.mnemos.event.domain.wipe.exception

import com.mnemos.common.web.exception.AppException
import org.springframework.http.HttpStatus

class WipeNotFoundException(
    message: String,
) : AppException(
        message = message,
        code = "WIPE_NOT_FOUND",
        httpStatus = HttpStatus.NOT_FOUND,
    )
