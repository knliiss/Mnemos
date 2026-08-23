package com.mnemos.social.application.spotlight.exception

import com.mnemos.common.web.exception.AppException
import org.springframework.http.HttpStatus

class SpotlightQueueLimitException :
    AppException(
        message = "Spotlight queue is longer than allowed",
        code = "SPOTLIGHT_QUEUE_LIMIT_EXCEEDED",
        httpStatus = HttpStatus.CONFLICT,
    )
