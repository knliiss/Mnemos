package com.mnemos.realtime.adapter.sse.protocol

import com.mnemos.realtime.application.delivery.model.RealtimeEvent

object RealtimeEventMapper {
    fun RealtimeEvent.toResponse(): RealtimeEventResponse =
        RealtimeEventResponse(
            id = id,
            type = type.eventName,
            occurredAt = occurredAt,
            operationId = operationId,
            payload = payload,
        )
}
