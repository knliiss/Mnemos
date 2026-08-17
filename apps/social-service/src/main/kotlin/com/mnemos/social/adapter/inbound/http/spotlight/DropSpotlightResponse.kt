package com.mnemos.social.adapter.inbound.http.spotlight

import java.time.Instant
import java.util.UUID

data class DropSpotlightResponse(
    val id: UUID,
    val itemDropEventId: UUID,
    val sourceAccountId: UUID,
    val pricePrime: Long,
    val scheduledAt: Instant,
    val status: String,
)
