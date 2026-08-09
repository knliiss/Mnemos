package com.mnemos.messaging.outbox.metadata

import com.mnemos.messaging.core.context.MessageContextHolder
import com.mnemos.messaging.core.event.EventMetadata
import org.slf4j.MDC
import java.util.UUID

class DefaultEventMetadataProvider : EventMetadataProvider {

    override fun current(): EventMetadata {
        val messageContext = MessageContextHolder.current()

        if (messageContext != null) {
            return messageContext.outgoingMetadata()
        }

        return EventMetadata(
            correlationId = MDC.get(CORRELATION_ID_KEY)?.takeIf(String::isNotBlank),
            causationId = resolveCausationId(),
        )
    }

    private fun resolveCausationId(): UUID? {
        val value = MDC.get(CAUSATION_ID_KEY)?.takeIf(String::isNotBlank) ?: return null

        return try {
            UUID.fromString(value)
        } catch (exception: IllegalArgumentException) {
            throw IllegalStateException(
                "MDC causationId must be a valid UUID: $value",
                exception,
            )
        }
    }

    private companion object {
        const val CORRELATION_ID_KEY = "requestId"
        const val CAUSATION_ID_KEY = "causationId"
    }
}