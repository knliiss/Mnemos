package com.mnemos.gateway.moderation

import org.springframework.cloud.gateway.filter.GatewayFilterChain
import org.springframework.cloud.gateway.filter.GlobalFilter
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.http.HttpStatus
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken
import org.springframework.stereotype.Component
import org.springframework.web.server.ServerWebExchange
import reactor.core.publisher.Mono
import java.util.UUID

@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 200)
class BanGlobalFilter(
    private val cache: BanCache,
) : GlobalFilter {
    override fun filter(
        exchange: ServerWebExchange,
        chain: GatewayFilterChain,
    ): Mono<Void> =
        exchange
            .getPrincipal<JwtAuthenticationToken>()
            .map { authentication ->
                authentication.token
                    .getClaimAsString(MNEMOS_USER_ID_CLAIM)
                    ?.let(::parseUserId)
                    ?.let(cache::isBanned)
                    ?: false
            }.defaultIfEmpty(false)
            .flatMap { banned ->
                if (banned) {
                    exchange.response.statusCode = HttpStatus.FORBIDDEN
                    exchange.response.setComplete()
                } else {
                    chain.filter(exchange)
                }
            }

    private fun parseUserId(value: String): UUID? =
        try {
            UUID.fromString(value)
        } catch (_: IllegalArgumentException) {
            null
        }

    private companion object {
        const val MNEMOS_USER_ID_CLAIM = "mnemos_user_id"
    }
}
