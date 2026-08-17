package com.mnemos.social.application.spotlight.port.out

import java.time.Instant

interface DropSpotlightQueue {
    fun reserve(now: Instant): DropSpotlightReservation
}
