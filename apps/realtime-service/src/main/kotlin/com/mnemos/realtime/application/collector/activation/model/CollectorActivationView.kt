package com.mnemos.realtime.application.collector.activation.model

import java.time.Instant

 data class CollectorActivationView(
    val token: String,
    val expiresAt: Instant,
)
