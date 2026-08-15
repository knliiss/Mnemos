package com.mnemos.messaging.kafka.transport

import com.mnemos.messaging.core.event.EventHeaders
import com.mnemos.messaging.core.transport.EventMessage
import com.mnemos.messaging.core.transport.EventTransport
import com.mnemos.messaging.kafka.security.KafkaMessageAuthenticationHeaders
import com.mnemos.messaging.kafka.security.KafkaMessageAuthenticator
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.header.internals.RecordHeader
import org.springframework.kafka.core.KafkaTemplate
import java.nio.charset.StandardCharsets
import java.util.concurrent.CompletionStage

class KafkaEventTransport(
    private val kafkaTemplate: KafkaTemplate<String, String>,
    private val messageAuthenticator: KafkaMessageAuthenticator? = null,
) : EventTransport {
    override fun publish(message: EventMessage): CompletionStage<Unit> {
        val record =
            ProducerRecord(
                message.destination.topic,
                message.destination.key,
                message.payload,
            )

        val headers =
            linkedMapOf(
                EventHeaders.CONTENT_TYPE to EventHeaders.CONTENT_TYPE_JSON,
            )

        headers.putAll(message.headers)

        messageAuthenticator?.let { authenticator ->
            val producer =
                message.headers[EventHeaders.PRODUCER]
                    ?: throw IllegalArgumentException(
                        "Authenticated Kafka messages must include ${EventHeaders.PRODUCER}",
                    )
            val signature =
                authenticator.sign(
                    producer = producer,
                    topic = message.destination.topic,
                    key = message.destination.key,
                    payload = message.payload,
                )

            headers[KafkaMessageAuthenticationHeaders.SIGNATURE_VERSION] =
                KafkaMessageAuthenticationHeaders.ED25519_V1
            headers[KafkaMessageAuthenticationHeaders.SIGNATURE] = signature
        }

        headers.forEach { (name, value) ->
            record.headers().add(
                RecordHeader(
                    name,
                    value.toByteArray(StandardCharsets.UTF_8),
                ),
            )
        }

        return kafkaTemplate
            .send(record)
            .thenApply { }
    }
}
