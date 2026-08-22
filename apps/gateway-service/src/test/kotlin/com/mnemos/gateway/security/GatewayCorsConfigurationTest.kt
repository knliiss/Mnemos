package com.mnemos.gateway.security

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.springframework.mock.http.server.reactive.MockServerHttpRequest
import org.springframework.mock.web.server.MockServerWebExchange

class GatewayCorsConfigurationTest {
    @Test
    fun `allows realtime session header for browser requests`() {
        val configuration = GatewayCorsConfiguration()
        val source = configuration.corsConfigurationSource(FRONTEND_ORIGIN)
        val exchange =
            MockServerWebExchange.from(
                MockServerHttpRequest
                    .options("https://api.knalis.rest/api/v1/realtime/events")
                    .header("Origin", FRONTEND_ORIGIN)
                    .header(
                        "Access-Control-Request-Headers",
                        "authorization,x-mnemos-realtime-session",
                    ).build(),
            )

        val cors = source.getCorsConfiguration(exchange)

        assertEquals(listOf(FRONTEND_ORIGIN), cors?.allowedOrigins)
        assertTrue(cors?.allowedHeaders?.contains("X-Mnemos-Realtime-Session") == true)
    }

    private companion object {
        const val FRONTEND_ORIGIN = "https://app.knalis.rest"
    }
}
