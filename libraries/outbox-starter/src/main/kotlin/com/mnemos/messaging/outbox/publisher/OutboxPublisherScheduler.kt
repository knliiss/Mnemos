package com.mnemos.messaging.outbox.publisher

import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled

class OutboxPublisherScheduler(
    private val publisher: OutboxEventPublisher,
) {

    @Scheduled(
        initialDelayString = "\${mnemos.outbox.publisher.initial-delay:1s}",
        fixedDelayString = "\${mnemos.outbox.publisher.fixed-delay:500ms}",
    )
    fun publish() {
        try {
            val processed = publisher.publishBatch()

            if (processed > 0) {
                logger.debug(
                    "Outbox publisher batch completed: processed={}",
                    processed,
                )
            }
        } catch (exception: Throwable) {
            logger.error(
                "Outbox publisher batch failed",
                exception,
            )
        }
    }

    private companion object {
        val logger = LoggerFactory.getLogger(OutboxPublisherScheduler::class.java)
    }
}