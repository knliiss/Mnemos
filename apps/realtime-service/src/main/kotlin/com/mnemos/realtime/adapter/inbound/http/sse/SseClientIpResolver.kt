package com.mnemos.realtime.adapter.inbound.http.sse

import jakarta.servlet.http.HttpServletRequest
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.nio.charset.StandardCharsets
import java.security.MessageDigest

@Component
class SseClientIpResolver(
    @Value("\${mnemos.realtime.gateway-forwarding-token}")
    private val gatewayForwardingToken: String,
) {
    init {
        require(gatewayForwardingToken.isNotBlank()) {
            "Realtime gateway forwarding token must not be blank"
        }
    }

    fun resolve(request: HttpServletRequest): String {
        val forwarded =
            if (hasTrustedForwardingToken(request)) {
                request
                    .getHeader(INTERNAL_CLIENT_IP_HEADER)
                    ?.trim()
                    ?.takeIf(::isValidAddressValue)
            } else {
                null
            }

        return forwarded
            ?: request.remoteAddr
                ?.trim()
                ?.takeIf(::isValidAddressValue)
            ?: UNKNOWN_ADDRESS
    }

    private fun hasTrustedForwardingToken(request: HttpServletRequest): Boolean {
        val suppliedToken = request.getHeader(INTERNAL_FORWARDING_TOKEN_HEADER) ?: return false

        return MessageDigest.isEqual(
            suppliedToken.toByteArray(StandardCharsets.UTF_8),
            gatewayForwardingToken.toByteArray(StandardCharsets.UTF_8),
        )
    }

    private fun isValidAddressValue(value: String): Boolean =
        value.isNotEmpty() &&
            value.length <= MAX_ADDRESS_LENGTH &&
            ',' !in value &&
            '\n' !in value &&
            '\r' !in value

    private companion object {
        const val INTERNAL_CLIENT_IP_HEADER = "X-Mnemos-Client-IP"
        const val INTERNAL_FORWARDING_TOKEN_HEADER = "X-Mnemos-Forwarding-Token"
        const val MAX_ADDRESS_LENGTH = 64
        const val UNKNOWN_ADDRESS = "unknown"
    }
}
