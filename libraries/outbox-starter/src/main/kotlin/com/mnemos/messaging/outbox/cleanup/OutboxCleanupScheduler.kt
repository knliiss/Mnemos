package com.mnemos.messaging.outbox.cleanup

import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled

class OutboxCleanupScheduler(
    private val cleanupService: OutboxCleanupService
) {
    @Scheduled(
        initialDelayString =
            "\${mnemos.outbox.cleanup.initial-delay:10m}",
        fixedDelayString =
            "\${mnemos.outbox.cleanup.fixed-delay:1h}"
    )
    fun cleanup() {
        try {
            val deleted = cleanupService.cleanup()

            if (deleted > 0) {
                logger.info(
                    "Deleted expired outbox events: count={}",
                    deleted
                )
            }
        } catch (exception: Throwable) {
            logger.error(
                "Outbox cleanup failed",
                exception
            )
        }
    }

    private companion object {
        val logger =
            LoggerFactory.getLogger(OutboxCleanupScheduler::class.java)
    }
}