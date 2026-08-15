package com.mnemos.realtime.application.collector.credential.model

import com.mnemos.realtime.domain.collector.model.CollectorCredentialId
import java.time.Instant

data class CollectorCredentialView(
    val id: CollectorCredentialId,
    val name: String,
    val enabled: Boolean,
    val createdAt: Instant,
    val updatedAt: Instant,
    val revokedAt: Instant?,
    val accessKey: String?,
)
