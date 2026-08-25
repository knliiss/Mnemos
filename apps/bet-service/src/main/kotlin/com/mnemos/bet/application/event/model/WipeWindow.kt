package com.mnemos.bet.application.event.model

import java.time.Instant

data class WipeWindow(
    val startedAt: Instant,
    val endedAt: Instant?,
)
