package com.mnemos.social.application.spotlight.exception

import com.mnemos.common.web.exception.AppException
import org.springframework.http.HttpStatus
import java.util.UUID

class DropSpotlightIdempotencyConflictException(
    idempotencyKey: UUID,
) : AppException(
        message = "Idempotency key is already used by another spotlight",
        code = "DROP_SPOTLIGHT_IDEMPOTENCY_CONFLICT",
        httpStatus = HttpStatus.CONFLICT,
        details = mapOf("idempotencyKey" to idempotencyKey),
    )
