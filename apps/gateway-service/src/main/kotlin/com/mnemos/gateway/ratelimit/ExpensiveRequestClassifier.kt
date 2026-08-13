package com.mnemos.gateway.ratelimit

import org.springframework.http.HttpMethod
import org.springframework.stereotype.Component
import org.springframework.web.server.ServerWebExchange

@Component
class ExpensiveRequestClassifier {
    fun isExpensive(exchange: ServerWebExchange): Boolean {
        val method = exchange.request.method

        if (method == HttpMethod.GET || method == HttpMethod.HEAD || method == HttpMethod.OPTIONS) {
            return false
        }

        val path = exchange.request.path.value()

        return path.startsWith(BET_API_PREFIX) || path.startsWith(MODERATION_API_PREFIX)
    }

    private companion object {
        const val BET_API_PREFIX = "/api/v1/bets/"
        const val MODERATION_API_PREFIX = "/api/v1/moderation/"
    }
}
