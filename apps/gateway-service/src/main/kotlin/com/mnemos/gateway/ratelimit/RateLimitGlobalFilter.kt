package com.mnemos.gateway.ratelimit

import org.springframework.cloud.gateway.filter.GatewayFilterChain
import org.springframework.cloud.gateway.filter.GlobalFilter
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import org.springframework.web.server.ServerWebExchange
import reactor.core.publisher.Mono

@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 100)
class RateLimitGlobalFilter(
    private val identityResolver: RateLimitIdentityResolver,
    private val policy: GatewayRateLimitPolicy,
    private val rateLimiter: RedisTokenBucketRateLimiter,
    private val expensiveRequestClassifier: ExpensiveRequestClassifier,
) : GlobalFilter {
    override fun filter(
        exchange: ServerWebExchange,
        chain: GatewayFilterChain,
    ): Mono<Void> =
        identityResolver
            .resolve(exchange)
            .flatMap { identity ->
                rateLimiter
                    .isAllowed(
                        bucketKey = "global:${identity.key}",
                        rule = policy.global(identity),
                    ).flatMap { globalAllowed ->
                        if (!globalAllowed) {
                            reject(exchange)
                        } else if (expensiveRequestClassifier.isExpensive(exchange)) {
                            filterExpensive(
                                exchange = exchange,
                                chain = chain,
                                identity = identity,
                            )
                        } else {
                            chain.filter(exchange)
                        }
                    }
            }

    private fun filterExpensive(
        exchange: ServerWebExchange,
        chain: GatewayFilterChain,
        identity: RateLimitIdentity,
    ): Mono<Void> =
        rateLimiter
            .isAllowed(
                bucketKey = "expensive:${identity.key}",
                rule = policy.expensive(identity),
            ).flatMap { allowed ->
                if (allowed) {
                    chain.filter(exchange)
                } else {
                    reject(exchange)
                }
            }

    private fun reject(exchange: ServerWebExchange): Mono<Void> {
        exchange.response.statusCode = HttpStatus.TOO_MANY_REQUESTS
        exchange.response.headers.set(HttpHeaders.RETRY_AFTER, RETRY_AFTER_SECONDS)
        return exchange.response.setComplete()
    }

    private companion object {
        const val RETRY_AFTER_SECONDS = "1"
    }
}
