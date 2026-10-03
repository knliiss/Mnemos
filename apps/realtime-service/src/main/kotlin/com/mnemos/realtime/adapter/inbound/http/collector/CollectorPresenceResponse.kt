package com.mnemos.realtime.adapter.inbound.http.collector

import java.util.UUID

data class CollectorPresenceResponse(
    val activeCollectors: Int,
    val revision: Long,
    val activeCollectorIds: Set<UUID>,
)
