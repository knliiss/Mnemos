package com.mnemos.realtime.adapter.inbound.websocket.collector.authentication

import com.mnemos.realtime.application.collector.security.AuthenticateCollectorService
import com.mnemos.realtime.application.collector.security.CollectorAccessKeyParser
import com.mnemos.realtime.domain.collector.model.CollectorCredentialId
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.server.ServerHttpRequest
import org.springframework.http.server.ServerHttpResponse
import org.springframework.web.socket.WebSocketHandler
import java.util.UUID

class CollectorHandshakeInterceptorTest {
    private val accessKeyParser = CollectorAccessKeyParser()
    private val authenticateCollectorService = mock<AuthenticateCollectorService>()
    private val interceptor =
        CollectorHandshakeInterceptor(
            accessKeyParser = accessKeyParser,
            authenticateCollectorService = authenticateCollectorService,
        )

    @Test
    fun `stores the authenticated collector id in the websocket session attributes`() {
        val collectorId =
            CollectorCredentialId(
                UUID.fromString("019c1129-ef54-7000-8000-000000000100"),
            )
        val secret = "a".repeat(43)
        val encodedAccessKey = "${collectorId.id}.$secret"
        val request = requestWithAuthorization("Collector $encodedAccessKey")
        val response = mock<ServerHttpResponse>()
        val attributes = mutableMapOf<String, Any>()
        val parsedAccessKey = requireNotNull(accessKeyParser.parse(encodedAccessKey))

        whenever(authenticateCollectorService.authenticate(parsedAccessKey)).thenReturn(collectorId)

        val accepted =
            interceptor.beforeHandshake(
                request = request,
                response = response,
                wsHandler = mock<WebSocketHandler>(),
                attributes = attributes,
            )

        assertTrue(accepted)
        assertEquals(
            collectorId.id,
            attributes[CollectorHandshakeInterceptor.COLLECTOR_ID_ATTRIBUTE],
        )
    }

    @Test
    fun `rejects a websocket handshake without a valid collector authorization`() {
        val request = requestWithAuthorization("Bearer invalid-token")
        val response = mock<ServerHttpResponse>()

        val accepted =
            interceptor.beforeHandshake(
                request = request,
                response = response,
                wsHandler = mock<WebSocketHandler>(),
                attributes = mutableMapOf(),
            )

        assertFalse(accepted)
        verify(response).setStatusCode(HttpStatus.UNAUTHORIZED)
    }

    private fun requestWithAuthorization(value: String): ServerHttpRequest {
        val request = mock<ServerHttpRequest>()
        val headers = HttpHeaders()
        headers.set(HttpHeaders.AUTHORIZATION, value)

        whenever(request.headers).thenReturn(headers)

        return request
    }
}
