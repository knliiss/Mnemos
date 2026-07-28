package com.mnemos.messaging.outbox.serialization

import com.mnemos.messaging.core.event.EventEnvelope
import tools.jackson.databind.json.JsonMapper

class JacksonEventEnvelopeSerializer(
    private val jsonMapper: JsonMapper
) : EventEnvelopeSerializer {

    override fun serialize(
        envelope: EventEnvelope<*>
    ): String = jsonMapper.writeValueAsString(envelope)
}