package com.mnemos.realtime.application.collector.activation

import com.mnemos.common.web.exception.AppException
import org.springframework.http.HttpStatus

class CollectorActivationConflictException :
    AppException(
        message = "Collector activation has already been redeemed by another installation",
        code = "COLLECTOR_ACTIVATION_CONFLICT",
        httpStatus = HttpStatus.CONFLICT,
    )
