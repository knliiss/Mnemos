package com.mnemos.realtime.botagent

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

class BotAgentHandshakeTest {
    private val registry = mock<BotAgentRegistry>()
    private val handshake = BotAgentHandshake(registry)

    @Test
    fun `valid bot agent authorization attaches the authenticated worker id`() {
        val agentId = UUID.fromString("00000000-0000-0000-0000-000000000001")
        val request = request("BotAgent " + "a".repeat(64))
        whenever(registry.authenticate("BotAgent " + "a".repeat(64))).thenReturn(agentId)
        val attributes = mutableMapOf<String, Any>()

        assertTrue(handshake.beforeHandshake(request, mock<ServerHttpResponse>(), mock<WebSocketHandler>(), attributes))
        assertEquals(agentId, attributes["agentId"])
    }

    @Test
    fun `missing bot authorization cannot open worker websocket`() {
        val response = mock<ServerHttpResponse>()
        val accepted = handshake.beforeHandshake(
            request("Bearer " + "a".repeat(64)),
            response,
            mock<WebSocketHandler>(),
            mutableMapOf(),
        )

        assertFalse(accepted)
        verify(response).setStatusCode(HttpStatus.UNAUTHORIZED)
    }

    private fun request(header: String): ServerHttpRequest {
        val request = mock<ServerHttpRequest>()
        whenever(request.headers).thenReturn(HttpHeaders().apply { set(HttpHeaders.AUTHORIZATION, header) })
        return request
    }
}
