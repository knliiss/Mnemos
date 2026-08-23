package com.mnemos.social.application.spotlight.exception

import com.mnemos.common.web.exception.AppException
import org.springframework.http.HttpStatus

class SpotlightBuyerCooldownException :
    AppException(
        message = "Spotlight buyer cooldown is active",
        code = "SPOTLIGHT_BUYER_COOLDOWN_ACTIVE",
        httpStatus = HttpStatus.TOO_MANY_REQUESTS,
    )
