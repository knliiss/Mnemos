package com.mnemos.security.handler

import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.HttpStatus
import org.springframework.security.core.AuthenticationException
import org.springframework.security.web.AuthenticationEntryPoint

class MnemosAuthenticationEntryPoint(
    private val responseWriter: SecurityErrorResponseWriter
) : AuthenticationEntryPoint {

    override fun commence(
        request: HttpServletRequest,
        response: HttpServletResponse,
        authException: AuthenticationException
    ) {
        responseWriter.write(
            request = request,
            response = response,
            code = "AUTHENTICATION_REQUIRED",
            message = "Authentication is required",
            httpStatus = HttpStatus.UNAUTHORIZED
        )
    }
}