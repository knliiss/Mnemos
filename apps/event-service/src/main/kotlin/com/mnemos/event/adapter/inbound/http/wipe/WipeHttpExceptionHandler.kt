package com.mnemos.event.adapter.inbound.http.wipe

import com.mnemos.event.domain.wipe.exception.WipeNotFoundException
import com.mnemos.event.domain.wipe.exception.WipeValidationException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class WipeHttpExceptionHandler {
    @ExceptionHandler(WipeNotFoundException::class)
    fun handleNotFound(exception: WipeNotFoundException): ResponseEntity<WipeErrorResponse> =
        ResponseEntity
            .status(HttpStatus.NOT_FOUND)
            .body(
                WipeErrorResponse(
                    code = "WIPE_NOT_FOUND",
                    message = exception.message ?: "Wipe was not found",
                ),
            )

    @ExceptionHandler(WipeValidationException::class)
    fun handleValidation(exception: WipeValidationException): ResponseEntity<WipeErrorResponse> =
        ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(
                WipeErrorResponse(
                    code = "INVALID_WIPE_BOUNDARY",
                    message = exception.message ?: "Wipe boundary is invalid",
                ),
            )
}
