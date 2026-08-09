package com.mnemos.messaging.inbox.cleanup

import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled

class InboxCleanupScheduler(
    private val cleanupService: InboxCleanupService,
) {
    @Scheduled(
        cron = "\${mnemos.inbox.cleanup.cron:0 0 4 * * SUN}",
        zone = "\${mnemos.inbox.cleanup.zone:UTC}",
    )
    fun cleanup() {
        try {
            val deleted = cleanupService.cleanup()

            if (deleted > 0) {
                logger.info("Deleted expired inbox events: count={}", deleted)
            }
        } catch (exception: Throwable) {
            logger.error("Inbox cleanup failed", exception)
        }
    }

    private companion object {
        val logger = LoggerFactory.getLogger(InboxCleanupScheduler::class.java)
    }
}