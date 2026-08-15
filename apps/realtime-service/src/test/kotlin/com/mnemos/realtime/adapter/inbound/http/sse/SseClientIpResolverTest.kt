package com.mnemos.realtime.adapter.inbound.http.sse

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.springframework.mock.web.MockHttpServletRequest

class SseClientIpResolverTest {
    private val resolver = SseClientIpResolver(TEST_FORWARDING_TOKEN)

    @Test
    fun `trusted gateway header is used as client ip`() {
        val request =
            MockHttpServletRequest().apply {
                remoteAddr = "172.20.0.5"
                addHeader(INTERNAL_CLIENT_IP_HEADER, "203.0.113.10")
                addHeader(INTERNAL_FORWARDING_TOKEN_HEADER, TEST_FORWARDING_TOKEN)
            }

        val result = resolver.resolve(request)

        assertEquals("203.0.113.10", result)
    }

    @Test
    fun `spoofed gateway header without trusted token falls back to direct peer address`() {
        val request =
            MockHttpServletRequest().apply {
                remoteAddr = "172.20.0.5"
                addHeader(INTERNAL_CLIENT_IP_HEADER, "203.0.113.10")
                addHeader(INTERNAL_FORWARDING_TOKEN_HEADER, "spoofed")
            }

        val result = resolver.resolve(request)

        assertEquals("172.20.0.5", result)
    }

    @Test
    fun `invalid gateway header falls back to direct peer address`() {
        val request =
            MockHttpServletRequest().apply {
                remoteAddr = "172.20.0.5"
                addHeader(INTERNAL_CLIENT_IP_HEADER, "203.0.113.10, 198.51.100.5")
                addHeader(INTERNAL_FORWARDING_TOKEN_HEADER, TEST_FORWARDING_TOKEN)
            }

        val result = resolver.resolve(request)

        assertEquals("172.20.0.5", result)
    }

    private companion object {
        const val INTERNAL_CLIENT_IP_HEADER = "X-Mnemos-Client-IP"
        const val INTERNAL_FORWARDING_TOKEN_HEADER = "X-Mnemos-Forwarding-Token"
        const val TEST_FORWARDING_TOKEN = "test-realtime-forwarding-token"
    }
}
