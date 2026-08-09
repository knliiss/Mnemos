package com.mnemos.messaging.core.message

import com.mnemos.messaging.core.event.EventMetadata
import java.time.Instant
import java.util.UUID

data class ConsumedMessage<T : Any>(
    val messageId: UUID,
    val type: String,
    val version: Int,
    val producer: String,
    val aggregateType: String,
    val aggregateId: UUID,
    val occurredAt: Instant,
    val metadata: EventMetadata,
    val payload: T,
)