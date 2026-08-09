package com.mnemos.messaging.core.event

import java.time.Instant
import java.util.UUID

data class IntegrationEvent<T : Any>(
    val type: String,
    val aggregateId: UUID,
    val occurredAt: Instant,
    val payload: T,
) {

    init {
        require(TYPE_PATTERN.matches(type)) {
            "Event type must use lowercase dot notation, for example account.created"
        }
    }

    private companion object {
        val TYPE_PATTERN =
            Regex("^[a-z][a-z0-9]*(?:\\.[a-z][a-z0-9-]*)+$")
    }
}