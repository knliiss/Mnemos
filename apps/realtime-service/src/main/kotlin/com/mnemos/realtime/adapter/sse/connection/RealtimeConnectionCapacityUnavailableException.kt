package com.mnemos.realtime.adapter.sse.connection

import com.mnemos.common.web.exception.AppException
import org.springframework.http.HttpStatus

class RealtimeConnectionCapacityUnavailableException :
    AppException(
        message = "Realtime connection capacity is temporarily unavailable",
        code = "REALTIME_CONNECTION_CAPACITY_UNAVAILABLE",
        httpStatus = HttpStatus.SERVICE_UNAVAILABLE,
        details = emptyMap(),
    )
