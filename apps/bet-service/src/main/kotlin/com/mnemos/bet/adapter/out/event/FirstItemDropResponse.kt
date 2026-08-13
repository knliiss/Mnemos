package com.mnemos.bet.adapter.out.event

import java.time.Instant

data class FirstItemDropResponse(
    val droppedFor: String,
    val occurredAt: Instant,
)
