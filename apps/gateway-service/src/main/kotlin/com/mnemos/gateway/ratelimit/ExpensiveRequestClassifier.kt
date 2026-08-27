package com.mnemos.gateway.ratelimit

import org.springframework.http.HttpMethod
import org.springframework.stereotype.Component
import org.springframework.web.server.ServerWebExchange

@Component
class ExpensiveRequestClassifier {
    fun isExpensive(exchange: ServerWebExchange): Boolean {
        val method = exchange.request.method
        val path = exchange.request.path.value()

        if (method == HttpMethod.GET && path == PLAYER_RESOLVE_PATH) {
            return true
        }

        if (
            method == HttpMethod.POST &&
            (
                path == PRESENCE_SUBSCRIPTIONS_PATH ||
                    path == PRIME_PURCHASES_PATH ||
                    isCoinPurchasePath(path)
            )
        ) {
            return true
        }

        if (method == HttpMethod.GET || method == HttpMethod.HEAD || method == HttpMethod.OPTIONS) {
            return false
        }

        return path.startsWith(BET_API_PREFIX) ||
            path.startsWith(MODERATION_API_PREFIX) ||
            path.startsWith(PERSONALIZATIONS_API_PREFIX)
    }

    private fun isCoinPurchasePath(path: String): Boolean {
        if (!path.startsWith(ACCOUNT_API_PREFIX) || !path.endsWith(COIN_PURCHASE_SUFFIX)) {
            return false
        }

        val accountId =
            path
                .removePrefix(ACCOUNT_API_PREFIX)
                .removeSuffix(COIN_PURCHASE_SUFFIX)

        return accountId.isNotBlank() && '/' !in accountId
    }

    private companion object {
        const val PLAYER_RESOLVE_PATH = "/api/v1/players/resolve"
        const val PRESENCE_SUBSCRIPTIONS_PATH = "/api/v1/player-presence-subscriptions"
        const val PRIME_PURCHASES_PATH = "/api/v1/payments/stars/prime/purchases"
        const val ACCOUNT_API_PREFIX = "/api/v1/accounts/"
        const val COIN_PURCHASE_SUFFIX = "/coin-purchases"
        const val BET_API_PREFIX = "/api/v1/bets/"
        const val MODERATION_API_PREFIX = "/api/v1/moderation/"
        const val PERSONALIZATIONS_API_PREFIX = "/api/v1/personalizations/"
    }
}
