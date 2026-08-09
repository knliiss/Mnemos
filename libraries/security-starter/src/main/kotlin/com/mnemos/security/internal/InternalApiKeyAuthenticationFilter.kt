package com.mnemos.security.internal

import com.mnemos.security.InternalSecurityProperties
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource
import org.springframework.web.filter.OncePerRequestFilter
import java.nio.charset.StandardCharsets
import java.security.MessageDigest

class InternalApiKeyAuthenticationFilter(
    private val properties: InternalSecurityProperties
) : OncePerRequestFilter() {

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        val serviceName = request
            .getHeader(properties.serviceHeader)
            ?.trim()
            ?.takeIf(String::isNotEmpty)

        val providedApiKey = request
            .getHeader(properties.apiKeyHeader)
            ?.takeIf(String::isNotEmpty)

        val expectedApiKey = serviceName?.let(properties.apiKeys::get)

        if (
            expectedApiKey != null &&
            providedApiKey != null &&
            apiKeysMatch(providedApiKey, expectedApiKey)
        ) {
            val authentication = UsernamePasswordAuthenticationToken(
                serviceName,
                null,
                listOf(SimpleGrantedAuthority(INTERNAL_SERVICE_AUTHORITY))
            )

            authentication.details =
                WebAuthenticationDetailsSource().buildDetails(request)

            val securityContext = SecurityContextHolder.createEmptyContext()
            securityContext.authentication = authentication
            SecurityContextHolder.setContext(securityContext)
        }

        filterChain.doFilter(request, response)
    }

    private fun apiKeysMatch(
        providedApiKey: String,
        expectedApiKey: String
    ): Boolean =
        MessageDigest.isEqual(
            providedApiKey.toByteArray(StandardCharsets.UTF_8),
            expectedApiKey.toByteArray(StandardCharsets.UTF_8)
        )

    companion object {
        const val INTERNAL_SERVICE_AUTHORITY = "INTERNAL_SERVICE"
    }
}