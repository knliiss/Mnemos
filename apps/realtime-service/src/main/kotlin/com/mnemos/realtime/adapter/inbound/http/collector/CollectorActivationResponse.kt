package com.mnemos.realtime.adapter.inbound.http.collector

import java.time.Instant

data class CollectorActivationResponse(
    val token: String,
    val expiresAt: Instant,
)
