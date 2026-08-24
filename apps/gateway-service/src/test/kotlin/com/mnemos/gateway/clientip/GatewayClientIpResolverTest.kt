package com.mnemos.gateway.clientip

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.springframework.mock.http.server.reactive.MockServerHttpRequest
import java.net.InetSocketAddress

class GatewayClientIpResolverTest {
    private val resolver = GatewayClientIpResolver()

    @Test
    fun `Cloudflare visitor address overrides proxy peer address`() {
        val request =
            MockServerHttpRequest
                .get("/api/v1/realtime/events")
                .remoteAddress(InetSocketAddress("192.0.2.10", 443))
                .header(CLOUDFLARE_CONNECTING_IP_HEADER, "203.0.113.42")
                .build()

        assertEquals("203.0.113.42", resolver.resolve(request))
    }

    @Test
    fun `malformed Cloudflare visitor address falls back to proxy peer address`() {
        val request =
            MockServerHttpRequest
                .get("/api/v1/realtime/events")
                .remoteAddress(InetSocketAddress("192.0.2.10", 443))
                .header(CLOUDFLARE_CONNECTING_IP_HEADER, "203.0.113.42, 198.51.100.7")
                .build()

        assertEquals("192.0.2.10", resolver.resolve(request))
    }

    private companion object {
        const val CLOUDFLARE_CONNECTING_IP_HEADER = "CF-Connecting-IP"
    }
}
