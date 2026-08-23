package com.mnemos.realtime.adapter.inbound.http.collector

data class CollectorPresenceResponse(
    val activeCollectors: Int,
    val revision: Long,
)
