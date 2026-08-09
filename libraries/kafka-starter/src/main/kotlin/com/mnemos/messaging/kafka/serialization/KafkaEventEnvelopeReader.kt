package com.mnemos.messaging.kafka.serialization

import com.mnemos.messaging.core.event.EventEnvelope
import tools.jackson.databind.json.JsonMapper

class KafkaEventEnvelopeReader(
    private val jsonMapper: JsonMapper
) {
    fun eventType(value: String): String =
        read(value, Any::class.java).eventType

    fun <T : Any> read(
        value: String,
        payloadType: Class<T>
    ): EventEnvelope<T> {
        val envelopeType = jsonMapper.typeFactory.constructParametricType(
            EventEnvelope::class.java,
            payloadType
        )

        return jsonMapper.readValue(value, envelopeType)
    }
}
