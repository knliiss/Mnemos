package com.mnemos.realtime.application.collector.activation

import com.mnemos.common.web.exception.AppException
import org.springframework.http.HttpStatus

class CollectorActivationNotFoundException : AppException(
    message = "Collector activation was not found",
    code = "COLLECTOR_ACTIVATION_NOT_FOUND",
    httpStatus = HttpStatus.NOT_FOUND,
)
