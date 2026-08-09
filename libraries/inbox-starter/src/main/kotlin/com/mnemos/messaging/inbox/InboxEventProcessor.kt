package com.mnemos.messaging.inbox

import org.springframework.transaction.support.TransactionOperations
import java.util.UUID

class InboxEventProcessor(
    private val repository: InboxEventRepository,
    private val transactionOperations: TransactionOperations,
    private val clock: InboxClock,
) {

    fun process(
        consumer: String,
        eventId: UUID,
        handler: () -> Unit,
    ): InboxProcessingResult {
        require(CONSUMER_PATTERN.matches(consumer)) {
            "Inbox consumer must use lowercase kebab or dot notation: $consumer"
        }

        return requireNotNull(
            transactionOperations.execute {
                val inserted =
                    repository.insertIfAbsent(
                        consumer = consumer,
                        eventId = eventId,
                        recordedAt = clock.now(),
                    )

                if (!inserted) {
                    return@execute InboxProcessingResult.DUPLICATE
                }

                handler()

                InboxProcessingResult.PROCESSED
            },
        )
    }

    private companion object {

        val CONSUMER_PATTERN =
            Regex(
                "^[a-z][a-z0-9]*(?:[.-][a-z0-9]+)*$",
            )
    }
}