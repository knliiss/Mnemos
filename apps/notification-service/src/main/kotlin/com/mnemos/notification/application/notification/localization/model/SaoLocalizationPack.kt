package com.mnemos.notification.application.notification.localization.model

data class SaoLocalizationPack(
    val locale: String,
    val hash: String,
    val properties: Map<String, String>,
)
