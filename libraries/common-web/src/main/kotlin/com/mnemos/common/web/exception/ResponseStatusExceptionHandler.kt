package com.mnemos.common.web.exception

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
import org.springframework.web.server.ResponseStatusException
import java.time.Clock
import java.time.Instant
import java.util.UUID

@Order(Ordered.HIGHEST_PRECEDENCE)
@RestControllerAdvice
class ResponseStatusExceptionHandler(
    private val clock: Clock = Clock.systemUTC(),
) {
    @ExceptionHandler(ResponseStatusException::class)
    fun handle(
        exception: ResponseStatusException,
        request: HttpServletRequest,
    ): ResponseEntity<ErrorResponse> {
        val status =
            HttpStatus.resolve(exception.statusCode.value())
                ?: HttpStatus.INTERNAL_SERVER_ERROR
        val response =
            ErrorResponse(
                code = status.name,
                message = exception.reason ?: status.reasonPhrase,
                statusCode = status.value(),
                details = emptyMap(),
                path = request.requestURI,
                requestId = resolveRequestId(),
                timestamp = Instant.now(clock),
            )

        return ResponseEntity(
            response,
            exception.headers,
            status,
        )
    }

    private fun resolveRequestId(): String =
        MDC.get(MdcKeys.REQUEST_ID)
            ?.takeIf(String::isNotBlank)
            ?: UUID.randomUUID().toString()
}
