package com.mnemos.gateway.ratelimit

import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.springframework.http.HttpMethod
import org.springframework.mock.http.server.reactive.MockServerHttpRequest
import org.springframework.mock.web.server.MockServerWebExchange

class ExpensiveRequestClassifierTest {
    private val classifier = ExpensiveRequestClassifier()

    @Test
    fun `player resolve is expensive even though it is a get request`() {
        assertTrue(isExpensive(HttpMethod.GET, "/api/v1/players/resolve"))
    }

    @Test
    fun `presence subscription creation is expensive`() {
        assertTrue(isExpensive(HttpMethod.POST, "/api/v1/player-presence-subscriptions"))
    }

    @Test
    fun `prime invoice creation is expensive`() {
        assertTrue(isExpensive(HttpMethod.POST, "/api/v1/payments/stars/prime/purchases"))
    }

    @Test
    fun `coin purchase is expensive`() {
        assertTrue(
            isExpensive(
                HttpMethod.POST,
                "/api/v1/accounts/01a02e75-540d-7371-8c3d-318c1fc50e0d/coin-purchases",
            ),
        )
    }

    @Test
    fun `read only payment catalog remains a normal request`() {
        assertFalse(isExpensive(HttpMethod.GET, "/api/v1/payments/stars/prime/catalog"))
    }

    private fun isExpensive(
        method: HttpMethod,
        path: String,
    ): Boolean {
        val request = MockServerHttpRequest.method(method, path).build()
        val exchange = MockServerWebExchange.from(request)

        return classifier.isExpensive(exchange)
    }
}
