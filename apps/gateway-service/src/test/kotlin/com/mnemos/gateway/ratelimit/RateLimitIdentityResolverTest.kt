package com.mnemos.gateway.ratelimit

import com.mnemos.gateway.clientip.GatewayClientIpResolver
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.springframework.mock.http.server.reactive.MockServerHttpRequest
import org.springframework.mock.web.server.MockServerWebExchange
import java.net.InetSocketAddress

class RateLimitIdentityResolverTest {
    private val resolver =
        RateLimitIdentityResolver(
            userRoleResolver = JwtUserRoleResolver(),
            clientIpResolver = GatewayClientIpResolver(),
        )

    @Test
    fun `anonymous identity uses Cloudflare visitor address`() {
        val request =
            MockServerHttpRequest
                .post("/api/v1/realtime/collector-activations/redeem")
                .remoteAddress(InetSocketAddress("192.0.2.10", 443))
                .header(CLOUDFLARE_CONNECTING_IP_HEADER, "203.0.113.42")
                .build()
        val exchange = MockServerWebExchange.from(request)

        val identity = requireNotNull(resolver.resolve(exchange).block())

        assertEquals("ip:203.0.113.42", identity.key)
        assertNull(identity.role)
    }

    private companion object {
        const val CLOUDFLARE_CONNECTING_IP_HEADER = "CF-Connecting-IP"
    }
}
