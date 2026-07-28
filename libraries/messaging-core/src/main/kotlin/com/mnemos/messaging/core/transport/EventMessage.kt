package com.mnemos.messaging.core.transport

data class EventMessage(
    val destination: EventDestination,
    val payload: String,
    val headers: Map<String, String> = emptyMap()
) {
    init {
        require(payload.isNotBlank()) {
            "Event payload cannot be blank"
        }
    }
}