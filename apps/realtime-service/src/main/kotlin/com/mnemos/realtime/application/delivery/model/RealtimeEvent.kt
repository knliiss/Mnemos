package com.mnemos.realtime.application.delivery.model

import java.time.Instant
import java.util.UUID

data class RealtimeEvent(
    val id: UUID,
    val userId: UUID,
    val type: RealtimeEventType,
    val occurredAt: Instant,
    val operationId: UUID?,
    val payload: RealtimeEventPayload,
) {
    init {
        require(type == payload.eventType()) {
            "Realtime event type does not match its payload"
        }

        when (type) {
            RealtimeEventType.WALLET_TRANSACTION_COMPLETED,
            RealtimeEventType.WALLET_TRANSACTION_FAILED,
            ->
                require(operationId == null) {
                    "Generic wallet events must not expose a domain operationId"
                }

            RealtimeEventType.BET_OPERATION_COMPLETED,
            RealtimeEventType.PERSONALIZATION_PURCHASE_COMPLETED,
            ->
                requireNotNull(operationId) {
                    "Domain operation events must expose their operationId"
                }
        }
    }
}
