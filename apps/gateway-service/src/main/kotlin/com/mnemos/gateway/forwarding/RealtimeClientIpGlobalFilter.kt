package com.mnemos.gateway.forwarding

import com.mnemos.gateway.clientip.GatewayClientIpResolver
import org.springframework.beans.factory.annotation.Value
import org.springframework.cloud.gateway.filter.GatewayFilterChain
import org.springframework.cloud.gateway.filter.GlobalFilter
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Component
import org.springframework.web.server.ServerWebExchange
import reactor.core.publisher.Mono

@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 50)
class RealtimeClientIpGlobalFilter(
    private val clientIpResolver: GatewayClientIpResolver,
    @Value("\${mnemos.gateway.realtime-forwarding-token}")
    private val forwardingToken: String,
) : GlobalFilter {
    init {
        require(forwardingToken.isNotBlank()) {
            "Realtime forwarding token must not be blank"
        }
    }

    override fun filter(
        exchange: ServerWebExchange,
        chain: GatewayFilterChain,
    ): Mono<Void> {
        val clientIp = clientIpResolver.resolve(exchange.request)
        val realtimeRequest =
            exchange.request.path
                .value()
                .startsWith(REALTIME_API_PREFIX)
        val request =
            exchange.request
                .mutate()
                .headers { headers ->
                    headers.remove(INTERNAL_CLIENT_IP_HEADER)
                    headers.remove(INTERNAL_FORWARDING_TOKEN_HEADER)

                    if (realtimeRequest) {
                        headers.set(INTERNAL_CLIENT_IP_HEADER, clientIp)
                        headers.set(INTERNAL_FORWARDING_TOKEN_HEADER, forwardingToken)
                    }
                }.build()

        return chain.filter(exchange.mutate().request(request).build())
    }

    private companion object {
        const val REALTIME_API_PREFIX = "/api/v1/realtime/"
        const val INTERNAL_CLIENT_IP_HEADER = "X-Mnemos-Client-IP"
        const val INTERNAL_FORWARDING_TOKEN_HEADER = "X-Mnemos-Forwarding-Token"
    }
}
