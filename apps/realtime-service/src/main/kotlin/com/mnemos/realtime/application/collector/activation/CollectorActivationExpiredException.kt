package com.mnemos.realtime.application.collector.activation

import com.mnemos.common.web.exception.AppException
import org.springframework.http.HttpStatus

class CollectorActivationExpiredException :
    AppException(
        message = "Collector activation has expired",
        code = "COLLECTOR_ACTIVATION_EXPIRED",
        httpStatus = HttpStatus.GONE,
    )
