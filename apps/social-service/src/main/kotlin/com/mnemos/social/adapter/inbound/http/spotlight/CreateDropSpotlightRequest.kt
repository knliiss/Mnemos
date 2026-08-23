package com.mnemos.social.adapter.inbound.http.spotlight

import java.util.UUID

data class CreateDropSpotlightRequest(
    val itemDropEventId: UUID,
    val idempotencyKey: UUID,
)
