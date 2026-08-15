package com.mnemos.personalization.application.badge.exception

import com.mnemos.common.web.exception.AppException
import org.springframework.http.HttpStatus

class BadgeNotOwnedException :
    AppException(
        "Badge is not owned by the user",
        "BADGE_NOT_OWNED",
        HttpStatus.CONFLICT,
    )
