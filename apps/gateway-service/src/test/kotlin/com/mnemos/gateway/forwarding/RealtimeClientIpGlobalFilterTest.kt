package com.mnemos.gateway.forwarding

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.springframework.cloud.gateway.filter.GatewayFilterChain
import org.springframework.mock.http.server.reactive.MockServerHttpRequest
import org.springframework.mock.web.server.MockServerWebExchange
import reactor.core.publisher.Mono
import java.net.InetSocketAddress

class RealtimeClientIpGlobalFilterTest {
    private val filter = RealtimeClientIpGlobalFilter(TEST_FORWARDING_TOKEN)

    @Test
    fun `realtime request replaces spoofed forwarding headers`() {
        val request =
            MockServerHttpRequest
                .get("/api/v1/realtime/events")
                .remoteAddress(InetSocketAddress("203.0.113.10", 443))
                .header(INTERNAL_CLIENT_IP_HEADER, "198.51.100.99")
                .header(INTERNAL_FORWARDING_TOKEN_HEADER, "spoofed")
                .build()
        val exchange = MockServerWebExchange.from(request)
        var forwardedClientIp: String? = null
        var forwardingToken: String? = null
        val chain =
            GatewayFilterChain { filteredExchange ->
                forwardedClientIp = filteredExchange.request.headers.getFirst(INTERNAL_CLIENT_IP_HEADER)
                forwardingToken = filteredExchange.request.headers.getFirst(INTERNAL_FORWARDING_TOKEN_HEADER)
                Mono.empty()
            }

        filter.filter(exchange, chain).block()

        assertEquals("203.0.113.10", forwardedClientIp)
        assertEquals(TEST_FORWARDING_TOKEN, forwardingToken)
    }

    @Test
    fun `non realtime request strips internal forwarding headers`() {
        val request =
            MockServerHttpRequest
                .get("/api/v1/users/me")
                .remoteAddress(InetSocketAddress("203.0.113.10", 443))
                .header(INTERNAL_CLIENT_IP_HEADER, "198.51.100.99")
                .header(INTERNAL_FORWARDING_TOKEN_HEADER, "spoofed")
                .build()
        val exchange = MockServerWebExchange.from(request)
        var forwardedClientIp: String? = "not-called"
        var forwardingToken: String? = "not-called"
        val chain =
            GatewayFilterChain { filteredExchange ->
                forwardedClientIp = filteredExchange.request.headers.getFirst(INTERNAL_CLIENT_IP_HEADER)
                forwardingToken = filteredExchange.request.headers.getFirst(INTERNAL_FORWARDING_TOKEN_HEADER)
                Mono.empty()
            }

        filter.filter(exchange, chain).block()

        assertNull(forwardedClientIp)
        assertNull(forwardingToken)
    }

    private companion object {
        const val INTERNAL_CLIENT_IP_HEADER = "X-Mnemos-Client-IP"
        const val INTERNAL_FORWARDING_TOKEN_HEADER = "X-Mnemos-Forwarding-Token"
        const val TEST_FORWARDING_TOKEN = "test-realtime-forwarding-token"
    }
}
