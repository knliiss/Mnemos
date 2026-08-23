package com.mnemos.event.adapter.inbound.http.internal

import java.time.Instant

data class CollectorCoverageRequest(
    val from: Instant,
    val to: Instant,
)
