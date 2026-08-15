package com.mnemos.realtime.application.collector.model

data class CollectorPresenceSnapshot(
    val activeCollectors: Int,
    val revision: Long,
)
