package com.mnemos.gateway.moderation

import org.springframework.cloud.gateway.filter.GatewayFilterChain
import org.springframework.cloud.gateway.filter.GlobalFilter
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken
import org.springframework.stereotype.Component
import org.springframework.web.server.ServerWebExchange
import reactor.core.publisher.Mono
import tools.jackson.databind.ObjectMapper
import java.util.Optional
import java.util.UUID

@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 200)
class BanGlobalFilter(
    private val cache: BanCache,
    private val objectMapper: ObjectMapper,
) : GlobalFilter {
    override fun filter(
        exchange: ServerWebExchange,
        chain: GatewayFilterChain,
    ): Mono<Void> {
        if (exchange.request.path.value() == REALTIME_EVENTS_PATH) {
            return chain.filter(exchange)
        }

        return exchange
            .getPrincipal<JwtAuthenticationToken>()
            .map { authentication ->
                Optional.ofNullable(
                    authentication.token
                        .getClaimAsString(MNEMOS_USER_ID_CLAIM)
                        ?.let(::parseUserId)
                        ?.let(cache::findActiveBan),
                )
            }.defaultIfEmpty(Optional.empty())
            .flatMap { ban ->
                if (ban.isPresent) {
                    writeBanResponse(exchange, ban.get())
                } else {
                    chain.filter(exchange)
                }
            }
    }

    private fun writeBanResponse(
        exchange: ServerWebExchange,
        ban: BanState,
    ): Mono<Void> {
        val response = exchange.response
        val body =
            objectMapper.writeValueAsBytes(
                mapOf(
                    "message" to "User access is suspended",
                    "statusCode" to HttpStatus.FORBIDDEN.value(),
                    "code" to USER_BANNED_CODE,
                    "details" to
                        mapOf(
                            "sanctionId" to ban.sanctionId.toString(),
                            "reason" to ban.reason,
                            "occurredAt" to ban.occurredAt.toString(),
                            "expiresAt" to ban.expiresAt?.toString(),
                        ),
                ),
            )

        response.statusCode = HttpStatus.FORBIDDEN
        response.headers.contentType = MediaType.APPLICATION_JSON
        response.headers.contentLength = body.size.toLong()

        return response.writeWith(
            Mono.just(response.bufferFactory().wrap(body)),
        )
    }

    private fun parseUserId(value: String): UUID? =
        try {
            UUID.fromString(value)
        } catch (_: IllegalArgumentException) {
            null
        }

    private companion object {
        const val MNEMOS_USER_ID_CLAIM = "mnemos_user_id"
        const val REALTIME_EVENTS_PATH = "/api/v1/realtime/events"
        const val USER_BANNED_CODE = "USER_BANNED"
    }
}
