package com.mnemos.messaging.kafka.consumer

import com.mnemos.messaging.core.context.MessageContext
import com.mnemos.messaging.core.context.MessageContextHolder
import com.mnemos.messaging.core.event.EventEnvelope
import com.mnemos.messaging.core.event.EventMetadata
import com.mnemos.messaging.core.message.ConsumedMessage
import com.mnemos.messaging.inbox.InboxEventProcessor
import com.mnemos.messaging.inbox.InboxProcessingResult
import com.mnemos.messaging.kafka.serialization.KafkaEventEnvelopeReader
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import java.time.Instant
import java.util.UUID
import java.util.concurrent.atomic.AtomicInteger

class KafkaMessageDispatcherTest {

    private val envelopeReader = mock<KafkaEventEnvelopeReader>()
    private val inboxEventProcessor = mock<InboxEventProcessor>()
    private val processed = mutableSetOf<Pair<String, UUID>>()

    @BeforeEach
    fun setUp() {
        processed.clear()

        whenever(
            inboxEventProcessor.process(
                consumer = any(),
                eventId = any(),
                handler = any(),
            ),
        ).thenAnswer { invocation ->
            val consumer = invocation.getArgument<String>(0)
            val eventId = invocation.getArgument<UUID>(1)
            val handler = invocation.getArgument<() -> Unit>(2)
            val key = consumer to eventId

            if (key in processed) {
                InboxProcessingResult.DUPLICATE
            } else {
                handler()
                processed += key
                InboxProcessingResult.PROCESSED
            }
        }
    }

    @Test
    fun `dispatch invokes matching handler with typed message`() {
        val envelope = envelope()
        prepareEnvelope(envelope)

        val handler = RecordingHandler(
            consumer = "wallet-service.account-created",
            definition = definition(),
        )

        val result = dispatcher(handler).dispatch(record(envelope))

        assertEquals(KafkaMessageDispatchResult.PROCESSED, result)
        assertEquals(1, handler.messages.size)
        assertEquals(envelope.eventId, handler.messages.single().messageId)
        assertEquals(envelope.aggregateId, handler.messages.single().aggregateId)
        assertEquals(envelope.payload, handler.messages.single().payload)
    }

    @Test
    fun `message context is available inside handler and cleared after`() {
        val envelope = envelope()
        prepareEnvelope(envelope)

        var context: MessageContext? = null

        val handler = RecordingHandler(
            consumer = "wallet-service.account-created",
            definition = definition(),
            action = {
                context = MessageContextHolder.current()
            },
        )

        dispatcher(handler).dispatch(record(envelope))

        assertEquals(envelope.eventId, context?.messageId)
        assertEquals(envelope.eventType, context?.type)
        assertEquals(envelope.aggregateId, context?.aggregateId)
        assertEquals(envelope.metadata, context?.metadata)
        assertNull(MessageContextHolder.current())
    }

    @Test
    fun `unknown message is ignored by default`() {
        whenever(envelopeReader.eventType(VALUE)).thenReturn("account.deleted")

        val result = dispatcher(
            RecordingHandler(
                consumer = "wallet-service.account-created",
                definition = definition(),
            ),
        ).dispatch(
            ConsumerRecord(TOPIC, 0, 0L, UUID.randomUUID().toString(), VALUE),
        )

        assertEquals(KafkaMessageDispatchResult.IGNORED, result)
    }

    @Test
    fun `unknown message is rejected when policy is reject`() {
        whenever(envelopeReader.eventType(VALUE)).thenReturn("wallet.unknown-command")

        assertThrows(KafkaMessageProtocolException::class.java) {
            dispatcher(
                RecordingHandler(
                    consumer = "wallet-service.account-created",
                    definition = definition(),
                ),
            ).dispatch(
                record = ConsumerRecord(TOPIC, 0, 0L, UUID.randomUUID().toString(), VALUE),
                unknownMessagePolicy = UnknownKafkaMessagePolicy.REJECT,
            )
        }
    }

    @Test
    fun `wrong version is rejected before handler`() {
        val envelope = envelope(version = 2)
        prepareEnvelope(envelope)

        val handler = RecordingHandler(
            consumer = "wallet-service.account-created",
            definition = definition(),
        )

        assertThrows(KafkaMessageProtocolException::class.java) {
            dispatcher(handler).dispatch(record(envelope))
        }

        assertEquals(0, handler.messages.size)
    }

    @Test
    fun `wrong aggregate type is rejected before handler`() {
        val envelope = envelope(aggregateType = "USER")
        prepareEnvelope(envelope)

        val handler = RecordingHandler(
            consumer = "wallet-service.account-created",
            definition = definition(),
        )

        assertThrows(KafkaMessageProtocolException::class.java) {
            dispatcher(handler).dispatch(record(envelope))
        }

        assertEquals(0, handler.messages.size)
    }

    @Test
    fun `wrong kafka key is rejected before handler`() {
        val envelope = envelope()
        prepareEnvelope(envelope)

        val handler = RecordingHandler(
            consumer = "wallet-service.account-created",
            definition = definition(),
        )

        assertThrows(KafkaMessageProtocolException::class.java) {
            dispatcher(handler).dispatch(
                ConsumerRecord(
                    TOPIC,
                    0,
                    0L,
                    UUID.randomUUID().toString(),
                    VALUE,
                ),
            )
        }

        assertEquals(0, handler.messages.size)
    }

    @Test
    fun `duplicate message does not invoke handler twice`() {
        val envelope = envelope()
        prepareEnvelope(envelope)

        val handler = RecordingHandler(
            consumer = "wallet-service.account-created",
            definition = definition(),
        )

        val dispatcher = dispatcher(handler)

        assertEquals(KafkaMessageDispatchResult.PROCESSED, dispatcher.dispatch(record(envelope)))
        assertEquals(KafkaMessageDispatchResult.DUPLICATE, dispatcher.dispatch(record(envelope)))
        assertEquals(1, handler.messages.size)
    }

    @Test
    fun `same message is processed by independent consumers`() {
        val envelope = envelope()
        prepareEnvelope(envelope)

        val first = RecordingHandler(
            consumer = "wallet-service.account-created-balances",
            definition = definition(),
        )

        val second = RecordingHandler(
            consumer = "wallet-service.account-created-statistics",
            definition = definition(),
        )

        val result = dispatcher(first, second).dispatch(record(envelope))

        assertEquals(KafkaMessageDispatchResult.PROCESSED, result)
        assertEquals(1, first.messages.size)
        assertEquals(1, second.messages.size)
    }

    @Test
    fun `successful handler is not repeated when another handler fails`() {
        val envelope = envelope()
        prepareEnvelope(envelope)

        val attempts = AtomicInteger()

        val first = RecordingHandler(
            consumer = "wallet-service.account-created-balances",
            definition = definition(),
        )

        val second = RecordingHandler(
            consumer = "wallet-service.account-created-statistics",
            definition = definition(),
            action = {
                if (attempts.getAndIncrement() == 0) {
                    throw IllegalStateException("temporary failure")
                }
            },
        )

        val dispatcher = dispatcher(first, second)

        assertThrows(IllegalStateException::class.java) {
            dispatcher.dispatch(record(envelope))
        }

        assertEquals(1, first.messages.size)
        assertEquals(1, second.messages.size)
        assertNull(MessageContextHolder.current())

        val retryResult = dispatcher.dispatch(record(envelope))

        assertEquals(KafkaMessageDispatchResult.PROCESSED, retryResult)
        assertEquals(1, first.messages.size)
        assertEquals(2, second.messages.size)
    }

    @Test
    fun `malformed envelope is converted to protocol exception`() {
        whenever(envelopeReader.eventType(VALUE)).thenThrow(
            IllegalArgumentException("invalid json"),
        )

        assertThrows(KafkaMessageProtocolException::class.java) {
            dispatcher(
                RecordingHandler(
                    consumer = "wallet-service.account-created",
                    definition = definition(),
                ),
            ).dispatch(
                ConsumerRecord(TOPIC, 0, 0L, UUID.randomUUID().toString(), VALUE),
            )
        }
    }

    private fun dispatcher(vararg handlers: KafkaMessageHandler<*>) =
        KafkaMessageDispatcher(
            envelopeReader = envelopeReader,
            inboxEventProcessor = inboxEventProcessor,
            handlers = handlers.toList(),
        )

    private fun definition() = KafkaMessageDefinition(
        topic = TOPIC,
        type = TYPE,
        version = 1,
        aggregateType = "ACCOUNT",
        payloadType = TestPayload::class.java,
    )

    private fun envelope(
        version: Int = 1,
        aggregateType: String = "ACCOUNT",
    ) = EventEnvelope(
        eventId = UUID.randomUUID(),
        eventType = TYPE,
        eventVersion = version,
        producer = "account-service",
        aggregateType = aggregateType,
        aggregateId = UUID.randomUUID(),
        occurredAt = Instant.parse("2026-08-09T00:00:00Z"),
        metadata = EventMetadata(correlationId = "request-1"),
        payload = TestPayload("Knalis"),
    )

    private fun prepareEnvelope(envelope: EventEnvelope<TestPayload>) {
        whenever(envelopeReader.eventType(VALUE)).thenReturn(TYPE)
        whenever(
            envelopeReader.read(
                value = VALUE,
                payloadType = TestPayload::class.java,
            ),
        ).thenReturn(envelope)
    }

    private fun record(envelope: EventEnvelope<TestPayload>) =
        ConsumerRecord(
            TOPIC,
            0,
            0L,
            envelope.aggregateId.toString(),
            VALUE,
        )

    private class RecordingHandler(
        override val consumer: String,
        override val definition: KafkaMessageDefinition<TestPayload>,
        private val action: (ConsumedMessage<TestPayload>) -> Unit = {},
    ) : KafkaMessageHandler<TestPayload> {

        val messages = mutableListOf<ConsumedMessage<TestPayload>>()

        override fun handle(message: ConsumedMessage<TestPayload>) {
            messages += message
            action(message)
        }
    }

    private data class TestPayload(
        val nickname: String,
    )

    private companion object {
        const val TOPIC = "mnemos.account.events.v1"
        const val TYPE = "account.created"
        const val VALUE = """{"eventType":"account.created"}"""
    }
}