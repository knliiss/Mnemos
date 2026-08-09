package com.mnemos.messaging.outbox.metadata

import com.mnemos.messaging.core.context.MessageContext
import com.mnemos.messaging.core.context.MessageContextHolder
import com.mnemos.messaging.core.event.EventMetadata
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import org.slf4j.MDC
import java.util.UUID

class DefaultEventMetadataProviderTest {

    private val provider = DefaultEventMetadataProvider()

    @AfterEach
    fun clearMdc() {
        MDC.clear()
    }

    @Test
    fun `message context has priority over mdc`() {
        val messageId = UUID.randomUUID()
        val actorUserId = UUID.randomUUID()

        MDC.put("requestId", "http-request")

        val context = MessageContext(
            messageId = messageId,
            type = "account.created",
            version = 1,
            producer = "account-service",
            aggregateType = "ACCOUNT",
            aggregateId = UUID.randomUUID(),
            metadata = EventMetadata(
                correlationId = "message-correlation",
                causationId = UUID.randomUUID(),
                actorUserId = actorUserId,
            ),
        )

        val metadata = MessageContextHolder.withContext(context) {
            provider.current()
        }

        assertEquals(
            EventMetadata(
                correlationId = "message-correlation",
                causationId = messageId,
                actorUserId = actorUserId,
            ),
            metadata,
        )
    }

    @Test
    fun `mdc is used when message context is absent`() {
        val causationId = UUID.randomUUID()

        MDC.put("requestId", "http-request")
        MDC.put("causationId", causationId.toString())

        assertEquals(
            EventMetadata(
                correlationId = "http-request",
                causationId = causationId,
            ),
            provider.current(),
        )
    }

    @Test
    fun `empty context returns empty metadata`() {
        assertEquals(EventMetadata(), provider.current())
    }

    @Test
    fun `invalid mdc causation id is rejected`() {
        MDC.put("causationId", "invalid")

        assertThrows(IllegalStateException::class.java) {
            provider.current()
        }
    }
}