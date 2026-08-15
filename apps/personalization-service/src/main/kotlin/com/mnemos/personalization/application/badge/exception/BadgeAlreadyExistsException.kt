package com.mnemos.personalization.application.badge.exception

import com.mnemos.common.web.exception.AppException
import org.springframework.http.HttpStatus

class BadgeAlreadyExistsException :
    AppException(
        "Badge with this code already exists",
        "BADGE_ALREADY_EXISTS",
        HttpStatus.CONFLICT,
    )
