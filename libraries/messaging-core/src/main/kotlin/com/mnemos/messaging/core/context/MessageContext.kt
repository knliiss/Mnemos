package com.mnemos.messaging.core.context

import com.mnemos.messaging.core.event.EventMetadata
import java.util.UUID

data class MessageContext(
    val messageId: UUID,
    val type: String,
    val version: Int,
    val producer: String,
    val aggregateType: String,
    val aggregateId: UUID,
    val metadata: EventMetadata,
) {
    fun outgoingMetadata(): EventMetadata = EventMetadata(
        correlationId = metadata.correlationId,
        causationId = messageId,
        actorUserId = metadata.actorUserId,
    )
}