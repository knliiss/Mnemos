package com.mnemos.messaging.kafka.transport

import com.mnemos.messaging.core.event.EventHeaders
import com.mnemos.messaging.core.transport.EventDestination
import com.mnemos.messaging.core.transport.EventMessage
import org.apache.kafka.clients.producer.ProducerRecord
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.kafka.support.SendResult
import java.nio.charset.StandardCharsets
import java.util.concurrent.CompletableFuture
import java.util.concurrent.CompletionException

class KafkaEventTransportTest {

    private val kafkaTemplate =
        mock<KafkaTemplate<String, String>>()

    private val transport =
        KafkaEventTransport(kafkaTemplate)

    @Test
    fun `publish sends topic key payload and headers`() {
        val sendFuture =
            CompletableFuture.completedFuture(
                mock<SendResult<String, String>>()
            )

        whenever(
            kafkaTemplate.send(
                any<ProducerRecord<String, String>>()
            )
        ).thenReturn(sendFuture)

        val message = EventMessage(
            destination = EventDestination(
                topic = "mnemos.account.events.v1",
                key = "account-1"
            ),
            payload = """{"eventType":"account.created"}""",
            headers = mapOf(
                EventHeaders.EVENT_ID to "event-1",
                EventHeaders.EVENT_TYPE to "account.created",
                EventHeaders.EVENT_VERSION to "1"
            )
        )

        transport
            .publish(message)
            .toCompletableFuture()
            .join()

        val recordCaptor =
            argumentCaptor<ProducerRecord<String, String>>()

        verify(kafkaTemplate).send(recordCaptor.capture())

        val record = recordCaptor.firstValue

        assertEquals(
            "mnemos.account.events.v1",
            record.topic()
        )

        assertEquals(
            "account-1",
            record.key()
        )

        assertEquals(
            """{"eventType":"account.created"}""",
            record.value()
        )

        assertEquals(
            EventHeaders.CONTENT_TYPE_JSON,
            record.headerValue(EventHeaders.CONTENT_TYPE)
        )

        assertEquals(
            "event-1",
            record.headerValue(EventHeaders.EVENT_ID)
        )

        assertEquals(
            "account.created",
            record.headerValue(EventHeaders.EVENT_TYPE)
        )

        assertEquals(
            "1",
            record.headerValue(EventHeaders.EVENT_VERSION)
        )
    }

    @Test
    fun `publish propagates kafka failure`() {
        val failure =
            IllegalStateException("Kafka broker unavailable")

        val sendFuture =
            CompletableFuture<SendResult<String, String>>()

        sendFuture.completeExceptionally(failure)

        whenever(
            kafkaTemplate.send(
                any<ProducerRecord<String, String>>()
            )
        ).thenReturn(sendFuture)

        val message = EventMessage(
            destination = EventDestination(
                topic = "mnemos.account.events.v1",
                key = "account-1"
            ),
            payload = """{"eventType":"account.created"}"""
        )

        val exception = assertThrows(
            CompletionException::class.java
        ) {
            transport
                .publish(message)
                .toCompletableFuture()
                .join()
        }

        assertSame(failure, exception.cause)
    }

    private fun ProducerRecord<String, String>.headerValue(
        name: String
    ): String {
        val header = headers().lastHeader(name)

        return String(
            header.value(),
            StandardCharsets.UTF_8
        )
    }
}
