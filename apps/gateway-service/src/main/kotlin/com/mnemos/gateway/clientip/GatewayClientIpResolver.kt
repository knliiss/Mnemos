package com.mnemos.gateway.clientip

import org.springframework.http.server.reactive.ServerHttpRequest
import org.springframework.stereotype.Component
import java.net.InetAddress
import java.net.InetSocketAddress

@Component
class GatewayClientIpResolver {
    fun resolve(request: ServerHttpRequest): String =
        resolveCloudflareAddress(
            request.headers.getFirst(CLOUDFLARE_CONNECTING_IP_HEADER),
        ) ?: resolvePeerAddress(request.remoteAddress)

    private fun resolveCloudflareAddress(rawAddress: String?): String? {
        val address =
            rawAddress
                ?.trim()
                ?.takeIf { candidate ->
                    candidate.isNotEmpty() &&
                        candidate.length <= MAX_ADDRESS_LENGTH &&
                        ',' !in candidate &&
                        '\r' !in candidate &&
                        '\n' !in candidate
                }
                ?: return null

        return if (':' in address) {
            normalizeIpv6Address(address)
        } else {
            normalizeIpv4Address(address)
        }
    }

    private fun normalizeIpv4Address(address: String): String? {
        val octets = address.split('.')

        if (octets.size != IPV4_OCTET_COUNT) {
            return null
        }

        val normalizedOctets = mutableListOf<String>()

        for (octet in octets) {
            if (
                octet.isEmpty() ||
                octet.length > IPV4_MAX_OCTET_LENGTH ||
                octet.any { character -> character !in '0'..'9' }
            ) {
                return null
            }

            val value =
                octet
                    .toIntOrNull()
                    ?.takeIf { value -> value in 0..IPV4_MAX_OCTET_VALUE }
                    ?: return null

            normalizedOctets += value.toString()
        }

        return normalizedOctets.joinToString(".")
    }

    private fun normalizeIpv6Address(address: String): String? {
        if (address.any { character -> !isIpv6Character(character) }) {
            return null
        }

        return runCatching {
            InetAddress.getByName(address).hostAddress
        }.getOrNull()
    }

    private fun isIpv6Character(character: Char): Boolean =
        character in '0'..'9' ||
            character.lowercaseChar() in 'a'..'f' ||
            character == ':' ||
            character == '.'

    private fun resolvePeerAddress(remoteAddress: InetSocketAddress?): String =
        remoteAddress?.address?.hostAddress
            ?: remoteAddress?.hostString
            ?: UNKNOWN_ADDRESS

    private companion object {
        const val CLOUDFLARE_CONNECTING_IP_HEADER = "CF-Connecting-IP"
        const val MAX_ADDRESS_LENGTH = 64
        const val IPV4_OCTET_COUNT = 4
        const val IPV4_MAX_OCTET_LENGTH = 3
        const val IPV4_MAX_OCTET_VALUE = 255
        const val UNKNOWN_ADDRESS = "unknown"
    }
}
