package com.mnemos.event.adapter.inbound.http.internal

import java.time.Instant

data class FirstItemDropResponse(
    val droppedFor: String,
    val occurredAt: Instant,
)
