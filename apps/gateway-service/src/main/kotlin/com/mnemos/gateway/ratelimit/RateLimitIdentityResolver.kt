package com.mnemos.gateway.ratelimit

import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken
import org.springframework.stereotype.Component
import org.springframework.web.server.ServerWebExchange
import reactor.core.publisher.Mono
import java.net.InetSocketAddress

@Component
class RateLimitIdentityResolver(
    private val userRoleResolver: JwtUserRoleResolver,
) {
    fun resolve(exchange: ServerWebExchange): Mono<RateLimitIdentity> =
        exchange
            .getPrincipal<JwtAuthenticationToken>()
            .map { authentication ->
                val token = authentication.token
                val userKey =
                    token
                        .getClaimAsString(MNEMOS_USER_ID_CLAIM)
                        ?.takeIf(String::isNotBlank)
                        ?.let { userId -> "user:$userId" }
                        ?: "keycloak:${token.subject}"

                RateLimitIdentity(
                    key = userKey,
                    role = userRoleResolver.resolve(token.claims[REALM_ACCESS_CLAIM]),
                )
            }.switchIfEmpty(
                Mono.fromSupplier {
                    RateLimitIdentity(
                        key = "ip:${resolveAddress(exchange.request.remoteAddress)}",
                        role = null,
                    )
                },
            )

    private fun resolveAddress(remoteAddress: InetSocketAddress?): String =
        remoteAddress?.address?.hostAddress
            ?: remoteAddress?.hostString
            ?: UNKNOWN_ADDRESS

    private companion object {
        const val MNEMOS_USER_ID_CLAIM = "mnemos_user_id"
        const val REALM_ACCESS_CLAIM = "realm_access"
        const val UNKNOWN_ADDRESS = "unknown"
    }
}
