package com.mnemos.event.adapter.inbound.http.internal

data class CollectorCoverageResponse(
    val covered: Boolean,
    val maximumActiveCollectors: Int,
)
