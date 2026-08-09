package com.mnemos.messaging.core.context

import com.mnemos.messaging.core.event.EventMetadata
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import java.util.UUID

class MessageContextHolderTest {

    @Test
    fun `context is available inside block and cleared after`() {
        val context = context()

        val current = MessageContextHolder.withContext(context) {
            MessageContextHolder.current()
        }

        assertEquals(context, current)
        assertNull(MessageContextHolder.current())
    }

    @Test
    fun `nested context restores previous context`() {
        val outer = context()
        val inner = context()

        MessageContextHolder.withContext(outer) {
            assertEquals(outer, MessageContextHolder.current())

            MessageContextHolder.withContext(inner) {
                assertEquals(inner, MessageContextHolder.current())
            }

            assertEquals(outer, MessageContextHolder.current())
        }

        assertNull(MessageContextHolder.current())
    }

    @Test
    fun `context is cleared when block fails`() {
        val context = context()

        assertThrows(IllegalStateException::class.java) {
            MessageContextHolder.withContext(context) {
                throw IllegalStateException("failure")
            }
        }

        assertNull(MessageContextHolder.current())
    }

    @Test
    fun `require current fails outside context`() {
        assertThrows(IllegalArgumentException::class.java) {
            MessageContextHolder.requireCurrent()
        }
    }

    @Test
    fun `outgoing metadata keeps correlation and actor and uses message id as causation`() {
        val messageId = UUID.randomUUID()
        val actorUserId = UUID.randomUUID()

        val context = MessageContext(
            messageId = messageId,
            type = "account.created",
            version = 1,
            producer = "account-service",
            aggregateType = "ACCOUNT",
            aggregateId = UUID.randomUUID(),
            metadata = EventMetadata(
                correlationId = "request-1",
                causationId = UUID.randomUUID(),
                actorUserId = actorUserId,
            ),
        )

        assertEquals(
            EventMetadata(
                correlationId = "request-1",
                causationId = messageId,
                actorUserId = actorUserId,
            ),
            context.outgoingMetadata(),
        )
    }

    private fun context() = MessageContext(
        messageId = UUID.randomUUID(),
        type = "account.created",
        version = 1,
        producer = "account-service",
        aggregateType = "ACCOUNT",
        aggregateId = UUID.randomUUID(),
        metadata = EventMetadata(correlationId = "request-1"),
    )
}