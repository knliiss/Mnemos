package com.mnemos.realtime.adapter.inbound.http.sse

import jakarta.servlet.http.HttpServletRequest
import org.springframework.stereotype.Component

@Component
class SseClientIpResolver {
    fun resolve(request: HttpServletRequest): String {
        val forwarded =
            request
                .getHeader(INTERNAL_CLIENT_IP_HEADER)
                ?.trim()
                ?.takeIf(::isValidAddressValue)

        return forwarded
            ?: request.remoteAddr
                ?.trim()
                ?.takeIf(::isValidAddressValue)
            ?: UNKNOWN_ADDRESS
    }

    private fun isValidAddressValue(value: String): Boolean =
        value.isNotEmpty() &&
            value.length <= MAX_ADDRESS_LENGTH &&
            ',' !in value &&
            '\n' !in value &&
            '\r' !in value

    private companion object {
        const val INTERNAL_CLIENT_IP_HEADER = "X-Mnemos-Client-IP"
        const val MAX_ADDRESS_LENGTH = 64
        const val UNKNOWN_ADDRESS = "unknown"
    }
}
