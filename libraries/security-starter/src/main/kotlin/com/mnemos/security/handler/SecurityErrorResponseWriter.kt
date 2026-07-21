package com.mnemos.security.handler

import com.mnemos.common.web.dto.ErrorResponse
import com.mnemos.common.web.logging.MdcKeys
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.MDC
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import tools.jackson.databind.ObjectMapper
import java.time.Clock
import java.time.Instant
import java.util.UUID

class SecurityErrorResponseWriter(
    private val objectMapper: ObjectMapper,
    private val clock: Clock = Clock.systemUTC()
) {

    fun write(
        request: HttpServletRequest,
        response: HttpServletResponse,
        code: String,
        message: String,
        httpStatus: HttpStatus
    ) {
        val errorResponse = ErrorResponse(
            code = code,
            message = message,
            statusCode = httpStatus.value(),
            details = emptyMap(),
            path = request.requestURI,
            requestId = resolveRequestId(),
            timestamp = Instant.now(clock)
        )

        response.status = httpStatus.value()
        response.contentType = MediaType.APPLICATION_JSON_VALUE
        response.characterEncoding = Charsets.UTF_8.name()

        objectMapper.writeValue(response.outputStream, errorResponse)
    }

    private fun resolveRequestId(): String {
        return MDC.get(MdcKeys.REQUEST_ID)
            ?.takeIf { it.isNotBlank() }
            ?: UUID.randomUUID().toString()
    }
}