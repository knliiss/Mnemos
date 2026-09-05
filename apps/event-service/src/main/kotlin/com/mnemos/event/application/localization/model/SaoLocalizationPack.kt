package com.mnemos.event.application.localization.model

import java.time.Instant

data class SaoLocalizationPack(
    val locale: String,
    val hash: String,
    val properties: Map<String, String>,
    val updatedAt: Instant,
)

data class SaoLocalizationPackDescriptor(
    val locale: String,
    val hash: String,
    val url: String,
)
