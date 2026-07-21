package com.mnemos.common.web.exception

import com.mnemos.common.web.dto.ErrorResponse
import com.mnemos.common.web.logging.MdcKeys
import jakarta.servlet.http.HttpServletRequest
import jakarta.validation.ConstraintViolationException
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.slf4j.MDC
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.validation.FieldError
import org.springframework.web.HttpMediaTypeNotAcceptableException
import org.springframework.web.HttpMediaTypeNotSupportedException
import org.springframework.web.HttpRequestMethodNotSupportedException
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.MissingRequestHeaderException
import org.springframework.web.bind.MissingServletRequestParameterException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException
import org.springframework.web.servlet.resource.NoResourceFoundException
import java.time.Clock
import java.time.Instant
import java.util.UUID

@RestControllerAdvice
class GlobalExceptionHandler(
    private val clock: Clock = Clock.systemUTC(),
    private val log: Logger = LoggerFactory.getLogger(GlobalExceptionHandler::class.java)
) {

    @ExceptionHandler(AppException::class)
    fun handleAppException(
        exception: AppException,
        request: HttpServletRequest
    ): ResponseEntity<ErrorResponse> = errorResponse(
        status = exception.httpStatus,
        code = exception.code,
        message = exception.message ?: "Application error",
        details = exception.details,
        request = request
    )

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidationException(
        exception: MethodArgumentNotValidException,
        request: HttpServletRequest
    ): ResponseEntity<ErrorResponse> {
        val fieldErrors = exception.bindingResult.fieldErrors
            .groupBy(FieldError::getField)
            .mapValues { (_, errors) ->
                errors.mapNotNull(FieldError::getDefaultMessage)
            }

        return errorResponse(
            status = HttpStatus.BAD_REQUEST,
            code = "VALIDATION_FAILED",
            message = "Request validation failed",
            details = mapOf("fields" to fieldErrors),
            request = request
        )
    }

    @ExceptionHandler(ConstraintViolationException::class)
    fun handleConstraintViolation(
        exception: ConstraintViolationException,
        request: HttpServletRequest
    ): ResponseEntity<ErrorResponse> {
        val violations = exception.constraintViolations
            .groupBy { it.propertyPath.toString() }
            .mapValues { (_, violations) -> violations.map { it.message } }

        return errorResponse(
            status = HttpStatus.BAD_REQUEST,
            code = "CONSTRAINT_VIOLATION",
            message = "Request parameters failed validation",
            details = mapOf("fields" to violations),
            request = request
        )
    }

    @ExceptionHandler(HttpMessageNotReadableException::class)
    fun handleUnreadableBody(
        exception: HttpMessageNotReadableException,
        request: HttpServletRequest
    ): ResponseEntity<ErrorResponse> = errorResponse(
        status = HttpStatus.BAD_REQUEST,
        code = "INVALID_REQUEST_BODY",
        message = "Request body is missing, malformed, or contains an invalid value",
        request = request
    )

    @ExceptionHandler(MethodArgumentTypeMismatchException::class)
    fun handleArgumentTypeMismatch(
        exception: MethodArgumentTypeMismatchException,
        request: HttpServletRequest
    ): ResponseEntity<ErrorResponse> = errorResponse(
        status = HttpStatus.BAD_REQUEST,
        code = "INVALID_PARAMETER",
        message = "Parameter '${exception.name}' has an invalid value",
        details = mapOf(
            "parameter" to exception.name,
            "value" to exception.value,
            "expectedType" to exception.requiredType?.simpleName
        ),
        request = request
    )

    @ExceptionHandler(MissingServletRequestParameterException::class)
    fun handleMissingRequestParameter(
        exception: MissingServletRequestParameterException,
        request: HttpServletRequest
    ): ResponseEntity<ErrorResponse> = errorResponse(
        status = HttpStatus.BAD_REQUEST,
        code = "MISSING_REQUEST_PARAMETER",
        message = "Required parameter '${exception.parameterName}' is missing",
        details = mapOf(
            "parameter" to exception.parameterName,
            "expectedType" to exception.parameterType
        ),
        request = request
    )

    @ExceptionHandler(MissingRequestHeaderException::class)
    fun handleMissingRequestHeader(
        exception: MissingRequestHeaderException,
        request: HttpServletRequest
    ): ResponseEntity<ErrorResponse> = errorResponse(
        status = HttpStatus.BAD_REQUEST,
        code = "MISSING_REQUEST_HEADER",
        message = "Required header '${exception.headerName}' is missing",
        details = mapOf("header" to exception.headerName),
        request = request
    )

    @ExceptionHandler(HttpRequestMethodNotSupportedException::class)
    fun handleMethodNotSupported(
        exception: HttpRequestMethodNotSupportedException,
        request: HttpServletRequest
    ): ResponseEntity<ErrorResponse> {
        val supportedMethods = exception.supportedHttpMethods.orEmpty()
        val headers = HttpHeaders().apply {
            if (supportedMethods.isNotEmpty()) {
                allow = supportedMethods
            }
        }

        return errorResponse(
            status = HttpStatus.METHOD_NOT_ALLOWED,
            code = "METHOD_NOT_ALLOWED",
            message = "Request method '${request.method}' is not supported for this endpoint",
            details = mapOf(
                "method" to request.method,
                "supportedMethods" to supportedMethods.map { it.name() }
            ),
            request = request,
            headers = headers
        )
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException::class)
    fun handleMediaTypeNotSupported(
        exception: HttpMediaTypeNotSupportedException,
        request: HttpServletRequest
    ): ResponseEntity<ErrorResponse> = errorResponse(
        status = HttpStatus.UNSUPPORTED_MEDIA_TYPE,
        code = "UNSUPPORTED_MEDIA_TYPE",
        message = "Content type '${exception.contentType}' is not supported",
        details = mapOf(
            "contentType" to exception.contentType?.toString(),
            "supportedMediaTypes" to exception.supportedMediaTypes.map { it.toString() }
        ),
        request = request
    )

    @ExceptionHandler(HttpMediaTypeNotAcceptableException::class)
    fun handleMediaTypeNotAcceptable(
        exception: HttpMediaTypeNotAcceptableException,
        request: HttpServletRequest
    ): ResponseEntity<ErrorResponse> = errorResponse(
        status = HttpStatus.NOT_ACCEPTABLE,
        code = "NOT_ACCEPTABLE",
        message = "The requested response media type is not supported",
        details = mapOf(
            "supportedMediaTypes" to exception.supportedMediaTypes.map { it.toString() }
        ),
        request = request
    )

    @ExceptionHandler(NoResourceFoundException::class)
    fun handleNoResourceFound(
        exception: NoResourceFoundException,
        request: HttpServletRequest
    ): ResponseEntity<ErrorResponse> = errorResponse(
        status = HttpStatus.NOT_FOUND,
        code = "ENDPOINT_NOT_FOUND",
        message = "No endpoint exists for '${request.method} ${request.requestURI}'",
        request = request
    )

    @ExceptionHandler(Exception::class)
    fun handleUnexpectedException(
        exception: Exception,
        request: HttpServletRequest
    ): ResponseEntity<ErrorResponse> {
        val requestId = resolveRequestId()

        log.error(
            "Unexpected error occurred. requestId={}, method={}, path={}",
            requestId,
            request.method,
            request.requestURI,
            exception
        )

        return errorResponse(
            status = HttpStatus.INTERNAL_SERVER_ERROR,
            code = "INTERNAL_SERVER_ERROR",
            message = "An unexpected error occurred",
            request = request,
            requestId = requestId
        )
    }

    private fun errorResponse(
        status: HttpStatus,
        code: String,
        message: String,
        request: HttpServletRequest,
        details: Map<String, Any?> = emptyMap(),
        headers: HttpHeaders = HttpHeaders(),
        requestId: String = resolveRequestId()
    ): ResponseEntity<ErrorResponse> {
        val response = ErrorResponse(
            code = code,
            message = message,
            statusCode = status.value(),
            details = details,
            path = request.requestURI,
            requestId = requestId,
            timestamp = Instant.now(clock)
        )

        return ResponseEntity(response, headers, status)
    }

    private fun resolveRequestId(): String =
        MDC.get(MdcKeys.REQUEST_ID)
            ?.takeIf { it.isNotBlank() }
            ?: UUID.randomUUID().toString()
}
