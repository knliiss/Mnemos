package com.mnemos.messaging.core.event

import java.time.Instant
import java.util.UUID

data class IntegrationEvent<T : Any>(
    val eventType: String,
    val eventVersion: Int,
    val aggregateType: String,
    val aggregateId: UUID,
    val occurredAt: Instant,
    val payload: T
) {
    init {
        require(EVENT_TYPE_PATTERN.matches(eventType)) {
            "Event type must use lowercase dot notation, for example account.created"
        }

        require(eventVersion > 0) {
            "Event version must be positive"
        }

        require(AGGREGATE_TYPE_PATTERN.matches(aggregateType)) {
            "Aggregate type must use uppercase snake case, for example ACCOUNT_ROLE"
        }
    }

    private companion object {
        val EVENT_TYPE_PATTERN =
            Regex("^[a-z][a-z0-9]*(?:\\.[a-z][a-z0-9-]*)+$")

        val AGGREGATE_TYPE_PATTERN =
            Regex("^[A-Z][A-Z0-9_]*$")
    }
}