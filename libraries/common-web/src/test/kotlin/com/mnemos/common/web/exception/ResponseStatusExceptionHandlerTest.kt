package com.mnemos.common.web.exception

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.springframework.http.HttpStatus
import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.web.server.ResponseStatusException
import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset

class ResponseStatusExceptionHandlerTest {
    @Test
    fun `preserves response status exception status and reason`() {
        val now = Instant.parse("2026-08-16T00:00:00Z")
        val handler =
            ResponseStatusExceptionHandler(
                clock = Clock.fixed(now, ZoneOffset.UTC),
            )
        val request = MockHttpServletRequest("GET", "/api/v1/profiles/unknown")
        val exception = ResponseStatusException(HttpStatus.NOT_FOUND, "Public profile was not found")

        val response = handler.handle(exception, request)
        val body = requireNotNull(response.body)

        assertEquals(HttpStatus.NOT_FOUND, response.statusCode)
        assertEquals("NOT_FOUND", body.code)
        assertEquals("Public profile was not found", body.message)
        assertEquals("/api/v1/profiles/unknown", body.path)
        assertEquals(now, body.timestamp)
    }
}
