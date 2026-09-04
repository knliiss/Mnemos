package com.mnemos.realtime.adapter.inbound.http.collector

import com.mnemos.realtime.application.collector.security.AuthenticateCollectorService
import com.mnemos.realtime.application.collector.security.CollectorAccessKeyParser
import com.mnemos.realtime.domain.collector.model.CollectorCredentialId
import org.junit.jupiter.api.Assertions.assertDoesNotThrow
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.springframework.http.HttpStatus
import org.springframework.web.server.ResponseStatusException
import java.util.UUID

class CollectorCredentialValidationControllerTest {
    private val accessKeyParser = CollectorAccessKeyParser()
    private val authenticateCollectorService = mock<AuthenticateCollectorService>()
    private val controller =
        CollectorCredentialValidationController(
            accessKeyParser = accessKeyParser,
            authenticateCollectorService = authenticateCollectorService,
        )

    @Test
    fun `accepts an active collector credential`() {
        val collectorId =
            CollectorCredentialId(
                UUID.fromString("019c1129-ef54-7000-8000-000000000100"),
            )
        val secret = "a".repeat(43)
        val encodedAccessKey = "${collectorId.id}.$secret"
        val accessKey = requireNotNull(accessKeyParser.parse(encodedAccessKey))

        whenever(authenticateCollectorService.authenticate(accessKey)).thenReturn(collectorId)

        assertDoesNotThrow {
            controller.validate("Collector $encodedAccessKey")
        }
    }

    @Test
    fun `rejects a revoked or unknown collector credential`() {
        val collectorId =
            CollectorCredentialId(
                UUID.fromString("019c1129-ef54-7000-8000-000000000101"),
            )
        val secret = "b".repeat(43)
        val encodedAccessKey = "${collectorId.id}.$secret"
        val accessKey = requireNotNull(accessKeyParser.parse(encodedAccessKey))

        whenever(authenticateCollectorService.authenticate(accessKey)).thenReturn(null)

        val exception =
            assertThrows(ResponseStatusException::class.java) {
                controller.validate("Collector $encodedAccessKey")
            }

        assertEquals(HttpStatus.UNAUTHORIZED, exception.statusCode)
    }

    @Test
    fun `rejects missing or non-collector authorization`() {
        val missing =
            assertThrows(ResponseStatusException::class.java) {
                controller.validate(null)
            }
        val bearer =
            assertThrows(ResponseStatusException::class.java) {
                controller.validate("Bearer token")
            }

        assertEquals(HttpStatus.UNAUTHORIZED, missing.statusCode)
        assertEquals(HttpStatus.UNAUTHORIZED, bearer.statusCode)
    }
}
