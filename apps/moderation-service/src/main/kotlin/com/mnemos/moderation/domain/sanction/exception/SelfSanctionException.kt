package com.mnemos.moderation.domain.sanction.exception

import com.mnemos.common.web.exception.AppException
import org.springframework.http.HttpStatus

class SelfSanctionException :
    AppException(
        message = "A user cannot sanction themselves",
        code = "SELF_SANCTION_NOT_ALLOWED",
        httpStatus = HttpStatus.BAD_REQUEST,
    )
