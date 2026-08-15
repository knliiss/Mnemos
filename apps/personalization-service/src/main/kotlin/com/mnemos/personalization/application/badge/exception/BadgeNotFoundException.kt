package com.mnemos.personalization.application.badge.exception

import com.mnemos.common.web.exception.AppException
import org.springframework.http.HttpStatus

class BadgeNotFoundException :
    AppException(
        "Badge was not found",
        "BADGE_NOT_FOUND",
        HttpStatus.NOT_FOUND,
    )
