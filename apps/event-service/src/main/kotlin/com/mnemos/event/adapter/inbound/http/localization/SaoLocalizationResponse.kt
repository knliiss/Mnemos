package com.mnemos.event.adapter.inbound.http.localization

import java.time.Instant

data class SaoLocalizationPackResponse(
    val locale: String,
    val hash: String,
    val properties: Map<String, String>,
    val updatedAt: Instant,
)

data class SaoLocalizationPackSummaryResponse(
    val locale: String,
    val hash: String,
    val updatedAt: Instant,
)
