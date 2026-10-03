package com.mnemos.realtime.application.collector.model

import com.mnemos.realtime.domain.collector.model.CollectorCredentialId

data class CollectorPresenceSnapshot(
    val activeCollectors: Int,
    val revision: Long,
    val activeCollectorIds: Set<CollectorCredentialId> = emptySet(),
)
