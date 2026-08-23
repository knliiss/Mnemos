package com.mnemos.personalization.application.badge.exception

import com.mnemos.common.web.exception.AppException
import org.springframework.http.HttpStatus

class InvalidBadgeImageException(
    message: String,
) : AppException(
        message,
        "INVALID_BADGE_IMAGE",
        HttpStatus.BAD_REQUEST,
    )
