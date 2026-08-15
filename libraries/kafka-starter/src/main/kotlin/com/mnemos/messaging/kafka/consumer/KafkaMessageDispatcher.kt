package com.mnemos.messaging.kafka.consumer

import com.mnemos.messaging.core.context.MessageContext
import com.mnemos.messaging.core.context.MessageContextHolder
import com.mnemos.messaging.core.event.EventEnvelope
import com.mnemos.messaging.core.event.EventHeaders
import com.mnemos.messaging.core.message.ConsumedMessage
import com.mnemos.messaging.kafka.security.KafkaMessageAuthenticationHeaders
import com.mnemos.messaging.kafka.security.KafkaMessageAuthenticator
import com.mnemos.messaging.kafka.serialization.KafkaEventEnvelopeReader
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.apache.kafka.common.header.Header
import org.slf4j.LoggerFactory
import java.nio.charset.StandardCharsets

class KafkaMessageDispatcher(
    private val envelopeReader: KafkaEventEnvelopeReader,
    private val messageProcessor: KafkaMessageProcessor,
    handlers: List<KafkaMessageHandler<*>>,
    private val messageAuthenticator: KafkaMessageAuthenticator? = null,
) {
    private val handlersByMessage =
        handlers
            .also(::validateHandlers)
            .groupBy { MessageKey(it.definition.topic, it.definition.type) }

    fun dispatch(
        record: ConsumerRecord<String, String>,
        unknownMessagePolicy: UnknownKafkaMessagePolicy = UnknownKafkaMessagePolicy.IGNORE,
    ): KafkaMessageDispatchResult {
        val authenticatedProducer = authenticate(record)
        val type = readMessageType(record.value())
        val handlers = handlersByMessage[MessageKey(record.topic(), type)]

        if (handlers == null) {
            return handleUnknownMessage(
                record = record,
                type = type,
                policy = unknownMessagePolicy,
            )
        }

        return dispatchToHandlers(
            record = record,
            handlers = handlers,
            authenticatedProducer = authenticatedProducer,
        )
    }

    @Suppress("UNCHECKED_CAST")
    private fun dispatchToHandlers(
        record: ConsumerRecord<String, String>,
        handlers: List<KafkaMessageHandler<*>>,
        authenticatedProducer: String?,
    ): KafkaMessageDispatchResult {
        val typedHandlers = handlers.map { it as KafkaMessageHandler<Any> }
        val definition = typedHandlers.first().definition
        val envelope = readEnvelope(record.value(), definition)

        validateEnvelope(record, envelope, definition)

        if (authenticatedProducer != null && envelope.producer != authenticatedProducer) {
            throw KafkaMessageProtocolException(
                "Authenticated producer does not match envelope producer: authenticated=$authenticatedProducer, envelope=${envelope.producer}",
            )
        }

        typedHandlers.forEach { validateDefinition(envelope, it.definition) }

        val message = envelope.toConsumedMessage()
        val context = envelope.toMessageContext()

        var processed = false
        var firstFailure: Exception? = null

        typedHandlers.forEach { handler ->
            try {
                val result =
                    messageProcessor.process(
                        consumer = handler.consumer,
                        eventId = envelope.eventId,
                    ) {
                        MessageContextHolder.withContext(context) {
                            handler.handle(message)
                        }
                    }

                when (result) {
                    KafkaMessageProcessingResult.PROCESSED -> {
                        processed = true

                        logger.debug(
                            "Processed Kafka message: topic={}, type={}, messageId={}, consumer={}",
                            record.topic(),
                            envelope.eventType,
                            envelope.eventId,
                            handler.consumer,
                        )
                    }

                    KafkaMessageProcessingResult.DUPLICATE -> {
                        logger.debug(
                            "Skipped duplicate Kafka message: topic={}, type={}, messageId={}, consumer={}",
                            record.topic(),
                            envelope.eventType,
                            envelope.eventId,
                            handler.consumer,
                        )
                    }
                }
            } catch (exception: Exception) {
                logger.warn(
                    "Kafka message handler failed: topic={}, type={}, messageId={}, consumer={}",
                    record.topic(),
                    envelope.eventType,
                    envelope.eventId,
                    handler.consumer,
                    exception,
                )

                if (firstFailure == null) {
                    firstFailure = exception
                } else {
                    firstFailure.addSuppressed(exception)
                }
            }
        }

        firstFailure?.let { throw it }

        return if (processed) {
            KafkaMessageDispatchResult.PROCESSED
        } else {
            KafkaMessageDispatchResult.DUPLICATE
        }
    }

    private fun authenticate(record: ConsumerRecord<String, String>): String? {
        val authenticator = messageAuthenticator ?: return null

        try {
            val producer = record.requireSingleHeader(EventHeaders.PRODUCER)
            val signatureVersion =
                record.requireSingleHeader(KafkaMessageAuthenticationHeaders.SIGNATURE_VERSION)
            val signature = record.requireSingleHeader(KafkaMessageAuthenticationHeaders.SIGNATURE)

            if (signatureVersion != KafkaMessageAuthenticationHeaders.ED25519_V1) {
                throw KafkaMessageProtocolException(
                    "Unsupported Kafka message signature version: $signatureVersion",
                )
            }

            if (
                !authenticator.verify(
                    producer = producer,
                    topic = record.topic(),
                    key = record.key(),
                    payload = record.value(),
                    encodedSignature = signature,
                )
            ) {
                throw KafkaMessageProtocolException(
                    "Kafka message signature verification failed for producer $producer",
                )
            }

            return producer
        } catch (exception: KafkaMessageProtocolException) {
            throw exception
        } catch (exception: Exception) {
            throw KafkaMessageProtocolException(
                message = "Kafka message authentication failed",
                cause = exception,
            )
        }
    }

    private fun ConsumerRecord<String, String>.requireSingleHeader(name: String): String {
        val values = headers().headers(name).toList()

        if (values.size != 1) {
            throw KafkaMessageProtocolException(
                "Kafka message must contain exactly one $name header",
            )
        }

        return values.single().asUtf8()
    }

    private fun Header.asUtf8(): String = String(value(), StandardCharsets.UTF_8)

    private fun readMessageType(value: String): String {
        return try {
            envelopeReader.eventType(value)
        } catch (exception: Exception) {
            throw KafkaMessageProtocolException(
                message = "Failed to read Kafka message type",
                cause = exception,
            )
        }
    }

    private fun <T : Any> readEnvelope(
        value: String,
        definition: KafkaMessageDefinition<T>,
    ): EventEnvelope<T> {
        return try {
            envelopeReader.read(
                value = value,
                payloadType = definition.payloadType,
            )
        } catch (exception: Exception) {
            throw KafkaMessageProtocolException(
                message = "Failed to deserialize ${definition.type}",
                cause = exception,
            )
        }
    }

    private fun <T : Any> validateEnvelope(
        record: ConsumerRecord<String, String>,
        envelope: EventEnvelope<T>,
        definition: KafkaMessageDefinition<T>,
    ) {
        if (record.topic() != definition.topic) {
            throw KafkaMessageProtocolException(
                "Unexpected topic for ${definition.type}: expected=${definition.topic}, actual=${record.topic()}",
            )
        }

        if (envelope.eventType != definition.type) {
            throw KafkaMessageProtocolException(
                "Unexpected message type: expected=${definition.type}, actual=${envelope.eventType}",
            )
        }

        if (envelope.eventVersion != definition.version) {
            throw KafkaMessageProtocolException(
                "Unsupported ${definition.type} version: expected=${definition.version}, actual=${envelope.eventVersion}",
            )
        }

        if (envelope.aggregateType != definition.aggregateType) {
            throw KafkaMessageProtocolException(
                "Unexpected aggregate type for ${definition.type}: expected=${definition.aggregateType}, actual=${envelope.aggregateType}",
            )
        }

        val expectedKey = envelope.aggregateId.toString()

        if (record.key() != expectedKey) {
            throw KafkaMessageProtocolException(
                "Unexpected Kafka key for ${definition.type}: expected=$expectedKey, actual=${record.key()}",
            )
        }
    }

    private fun <T : Any> validateDefinition(
        envelope: EventEnvelope<T>,
        definition: KafkaMessageDefinition<T>,
    ) {
        try {
            definition.validate(envelope)
        } catch (exception: KafkaMessageProtocolException) {
            throw exception
        } catch (exception: Exception) {
            throw KafkaMessageProtocolException(
                message = "Additional protocol validation failed for ${definition.type}",
                cause = exception,
            )
        }
    }

    private fun handleUnknownMessage(
        record: ConsumerRecord<String, String>,
        type: String,
        policy: UnknownKafkaMessagePolicy,
    ): KafkaMessageDispatchResult {
        if (policy == UnknownKafkaMessagePolicy.REJECT) {
            throw KafkaMessageProtocolException(
                "Unsupported Kafka message: topic=${record.topic()}, type=$type",
            )
        }

        logger.debug(
            "Ignoring unsupported Kafka message: topic={}, type={}, partition={}, offset={}",
            record.topic(),
            type,
            record.partition(),
            record.offset(),
        )

        return KafkaMessageDispatchResult.IGNORED
    }

    private fun validateHandlers(handlers: List<KafkaMessageHandler<*>>) {
        handlers.forEach { handler ->
            require(CONSUMER_PATTERN.matches(handler.consumer)) {
                "Invalid Kafka message consumer: ${handler.consumer}"
            }
        }

        val duplicateConsumers =
            handlers
                .groupBy { it.consumer }
                .filterValues { it.size > 1 }

        require(duplicateConsumers.isEmpty()) {
            "Duplicate Kafka message consumers registered: ${duplicateConsumers.keys.joinToString()}"
        }

        handlers
            .groupBy { MessageKey(it.definition.topic, it.definition.type) }
            .values
            .forEach(::validateCompatibleDefinitions)
    }

    private fun validateCompatibleDefinitions(handlers: List<KafkaMessageHandler<*>>) {
        if (handlers.size <= 1) {
            return
        }

        val first = handlers.first().definition

        handlers.drop(1).forEach { handler ->
            val current = handler.definition

            require(
                current.version == first.version &&
                    current.aggregateType == first.aggregateType &&
                    current.payloadType == first.payloadType,
            ) {
                "Handlers for ${first.topic}/${first.type} must use the same version, aggregate type and payload type"
            }
        }
    }

    private fun <T : Any> EventEnvelope<T>.toConsumedMessage(): ConsumedMessage<T> =
        ConsumedMessage(
            messageId = eventId,
            type = eventType,
            version = eventVersion,
            producer = producer,
            aggregateType = aggregateType,
            aggregateId = aggregateId,
            occurredAt = occurredAt,
            metadata = metadata,
            payload = payload,
        )

    private fun EventEnvelope<*>.toMessageContext(): MessageContext =
        MessageContext(
            messageId = eventId,
            type = eventType,
            version = eventVersion,
            producer = producer,
            aggregateType = aggregateType,
            aggregateId = aggregateId,
            metadata = metadata,
        )

    private data class MessageKey(
        val topic: String,
        val type: String,
    )

    private companion object {
        val CONSUMER_PATTERN = Regex("^[a-z][a-z0-9]*(?:[.-][a-z0-9]+)*$")
        val logger = LoggerFactory.getLogger(KafkaMessageDispatcher::class.java)
    }
}
