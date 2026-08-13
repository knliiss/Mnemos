package com.mnemos.bet.application.event.model

import java.time.Instant

data class FirstMatchingItemDrop(
    val droppedFor: String,
    val occurredAt: Instant,
)
