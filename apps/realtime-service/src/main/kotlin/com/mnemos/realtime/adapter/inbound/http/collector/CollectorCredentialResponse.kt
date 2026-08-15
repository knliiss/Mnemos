package com.mnemos.realtime.adapter.inbound.http.collector

import java.time.Instant
import java.util.UUID

data class CollectorCredentialResponse(
    val id: UUID,
    val name: String,
    val enabled: Boolean,
    val createdAt: Instant,
    val updatedAt: Instant,
    val revokedAt: Instant?,
    val accessKey: String?,
)
