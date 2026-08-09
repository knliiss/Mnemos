package com.mnemos.messaging.kafka.transport

import com.mnemos.messaging.core.event.EventHeaders
import com.mnemos.messaging.core.transport.EventMessage
import com.mnemos.messaging.core.transport.EventTransport
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.header.internals.RecordHeader
import org.springframework.kafka.core.KafkaTemplate
import java.nio.charset.StandardCharsets
import java.util.concurrent.CompletionStage

class KafkaEventTransport(
    private val kafkaTemplate: KafkaTemplate<String, String>
) : EventTransport {

    override fun publish(message: EventMessage): CompletionStage<Unit> {
        val record = ProducerRecord(
            message.destination.topic,
            message.destination.key,
            message.payload
        )

        val headers = linkedMapOf(
            EventHeaders.CONTENT_TYPE to EventHeaders.CONTENT_TYPE_JSON
        )

        headers.putAll(message.headers)

        headers.forEach { (name, value) ->
            record.headers().add(
                RecordHeader(
                    name,
                    value.toByteArray(StandardCharsets.UTF_8)
                )
            )
        }

        return kafkaTemplate
            .send(record)
            .thenApply { Unit }
    }
}
