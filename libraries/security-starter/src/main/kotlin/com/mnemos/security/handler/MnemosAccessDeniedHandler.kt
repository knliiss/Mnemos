package com.mnemos.security.handler

import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.HttpStatus
import org.springframework.security.access.AccessDeniedException
import org.springframework.security.web.access.AccessDeniedHandler

class MnemosAccessDeniedHandler(
    private val responseWriter: SecurityErrorResponseWriter
) : AccessDeniedHandler {

    override fun handle(
        request: HttpServletRequest,
        response: HttpServletResponse,
        accessDeniedException: AccessDeniedException
    ) {
        responseWriter.write(
            request = request,
            response = response,
            code = "ACCESS_DENIED",
            message = "You do not have permission to access this resource",
            httpStatus = HttpStatus.FORBIDDEN
        )
    }
}