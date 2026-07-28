package com.mnemos.messaging.core.event

import java.util.UUID

data class EventMetadata(
    val correlationId: String? = null,
    val causationId: UUID? = null,
    val actorUserId: UUID? = null
)