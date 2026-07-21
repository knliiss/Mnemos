package com.mnemos.common.web.logging

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.web.filter.OncePerRequestFilter
import org.slf4j.MDC
import java.util.UUID

class RequestIdFilter : OncePerRequestFilter() {
    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        val requestId = resolveRequestId(request)
        try {
            MDC.put(MdcKeys.REQUEST_ID, requestId)
            response.setHeader(MnemosHeaders.REQUEST_ID, requestId)
            filterChain.doFilter(request, response)
        } finally {
            MDC.remove(MdcKeys.REQUEST_ID)
        }
    }

    private fun resolveRequestId(request: HttpServletRequest): String {
        val incomingRequestId = request.getHeader(MnemosHeaders.REQUEST_ID)?.trim()
        return incomingRequestId?.takeIf { isValidRequestId(it) } ?: UUID.randomUUID().toString()
    }

    private fun isValidRequestId(requestId: String): Boolean {
        return requestId.isNotBlank() && requestId.length <= MAX_REQUEST_ID_LENGTH &&
                requestId.all { it.isLetterOrDigit() || it == '-' || it == '_' }
    }

    private companion object {
        const val MAX_REQUEST_ID_LENGTH = 128
    }

}