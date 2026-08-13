package com.mnemos.event.application.itemdrop.model

import java.time.Instant

data class FirstItemDropView(
    val droppedFor: String,
    val occurredAt: Instant,
)
