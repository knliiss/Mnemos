package com.mnemos.realtime.adapter.sse.connection

import com.mnemos.common.web.exception.AppException
import org.springframework.http.HttpStatus

class RealtimeConnectionLimitExceededException(
    limit: Int,
    scope: String,
) : AppException(
        message = "Realtime connection limit exceeded",
        code = "REALTIME_CONNECTION_LIMIT_EXCEEDED",
        httpStatus = HttpStatus.TOO_MANY_REQUESTS,
        details =
            mapOf(
                "scope" to scope,
                "limit" to limit,
            ),
    )
