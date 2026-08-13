package com.mnemos.moderation.domain.sanction.exception

import com.mnemos.common.web.exception.AppException
import org.springframework.http.HttpStatus

class SanctionDurationExceededException :
    AppException(
        message = "The requested sanction duration is not allowed for this role",
        code = "SANCTION_DURATION_NOT_ALLOWED",
        httpStatus = HttpStatus.BAD_REQUEST,
    )
