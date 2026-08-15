package com.mnemos.gateway.forwarding

import org.springframework.cloud.gateway.filter.GatewayFilterChain
import org.springframework.cloud.gateway.filter.GlobalFilter
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Component
import org.springframework.web.server.ServerWebExchange
import reactor.core.publisher.Mono
import java.net.InetSocketAddress

@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 50)
class RealtimeClientIpGlobalFilter : GlobalFilter {
    override fun filter(
        exchange: ServerWebExchange,
        chain: GatewayFilterChain,
    ): Mono<Void> {
        val clientIp = resolveAddress(exchange.request.remoteAddress)
        val realtimeRequest = exchange.request.path.value().startsWith(REALTIME_API_PREFIX)
        val request =
            exchange.request
                .mutate()
                .headers { headers ->
                    headers.remove(INTERNAL_CLIENT_IP_HEADER)

                    if (realtimeRequest) {
                        headers.set(INTERNAL_CLIENT_IP_HEADER, clientIp)
                    }
                }.build()

        return chain.filter(exchange.mutate().request(request).build())
    }

    private fun resolveAddress(remoteAddress: InetSocketAddress?): String =
        remoteAddress?.address?.hostAddress
            ?: remoteAddress?.hostString
            ?: UNKNOWN_ADDRESS

    private companion object {
        const val REALTIME_API_PREFIX = "/api/v1/realtime/"
        const val INTERNAL_CLIENT_IP_HEADER = "X-Mnemos-Client-IP"
        const val UNKNOWN_ADDRESS = "unknown"
    }
}
