package com.mnemos.messaging.outbox.serialization

import com.mnemos.messaging.core.event.EventEnvelope

fun interface EventEnvelopeSerializer {
    fun serialize(envelope: EventEnvelope<*>): String
}