package com.mnemos.messaging.outbox.publisher

import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled

class OutboxPublisherScheduler(
    private val publisher: OutboxEventPublisher
) {
    @Scheduled(
        initialDelayString =
            "\${mnemos.outbox.publisher.initial-delay:1s}",
        fixedDelayString =
            "\${mnemos.outbox.publisher.fixed-delay:500ms}"
    )
    fun publish() {
        try {
            publisher.publishBatch()
        } catch (exception: Throwable) {
            logger.error(
                "Outbox publisher batch failed",
                exception
            )
        }
    }

    private companion object {
        val logger =
            LoggerFactory.getLogger(OutboxPublisherScheduler::class.java)
    }
}