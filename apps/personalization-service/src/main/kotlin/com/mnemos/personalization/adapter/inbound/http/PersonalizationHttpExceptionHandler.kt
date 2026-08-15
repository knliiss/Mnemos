package com.mnemos.personalization.adapter.inbound.http

import com.mnemos.common.web.dto.ErrorResponse
import com.mnemos.common.web.logging.MdcKeys
import jakarta.servlet.http.HttpServletRequest
import org.slf4j.MDC
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import java.time.Clock
import java.time.Instant
import java.util.UUID

@Order(Ordered.HIGHEST_PRECEDENCE)
@RestControllerAdvice(basePackages = ["com.mnemos.personalization.adapter.inbound.http"])
class PersonalizationHttpExceptionHandler(
    private val clock: Clock,
) {
    @ExceptionHandler(IllegalArgumentException::class)
    fun handleInvalidPersonalization(
        exception: IllegalArgumentException,
        request: HttpServletRequest,
    ): ResponseEntity<ErrorResponse> {
        val response =
            ErrorResponse(
                code = "PERSONALIZATION_VALIDATION_FAILED",
                message = exception.message ?: "Personalization request is invalid",
                statusCode = HttpStatus.BAD_REQUEST.value(),
                details = emptyMap(),
                path = request.requestURI,
                requestId = resolveRequestId(),
                timestamp = Instant.now(clock),
            )

        return ResponseEntity.badRequest().body(response)
    }

    private fun resolveRequestId(): String =
        MDC.get(MdcKeys.REQUEST_ID)
            ?.takeIf { it.isNotBlank() }
            ?: UUID.randomUUID().toString()
}
