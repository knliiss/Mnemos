package com.mnemos.social.application.spotlight.port.out

import java.time.Instant

data class DropSpotlightReservation(
    val sequence: Long,
    val scheduledAt: Instant,
)
