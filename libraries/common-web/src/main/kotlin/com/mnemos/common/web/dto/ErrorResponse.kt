package com.mnemos.common.web.dto

import java.time.Instant

data class ErrorResponse(
    val message: String,
    val statusCode: Int,
    val code: String,
    val details: Map<String, Any?>,
    val path: String,
    val requestId: String,
    val timestamp: Instant,
)