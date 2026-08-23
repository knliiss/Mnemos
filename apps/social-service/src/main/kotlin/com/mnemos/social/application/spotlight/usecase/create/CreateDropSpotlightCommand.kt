package com.mnemos.social.application.spotlight.usecase.create

import java.util.UUID

data class CreateDropSpotlightCommand(
    val itemDropEventId: UUID,
    val idempotencyKey: UUID? = null,
)
