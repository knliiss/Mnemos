package com.mnemos.notification.adapter.out.event

import java.time.Instant

data class NotificationSaoLocalizationResponse(
    val locale: String,
    val hash: String,
    val properties: Map<String, String>,
    val updatedAt: Instant,
)
