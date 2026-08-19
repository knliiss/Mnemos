package com.mnemos.realtime.application.broadcast.exception

import com.mnemos.common.web.exception.AppException
import org.springframework.http.HttpStatus

class RealtimeBroadcastNotFoundException(
    sequence: Long,
) : AppException(
        message = "Realtime broadcast was not found",
        code = "REALTIME_BROADCAST_NOT_FOUND",
        httpStatus = HttpStatus.NOT_FOUND,
        details = mapOf("sequence" to sequence),
    )
