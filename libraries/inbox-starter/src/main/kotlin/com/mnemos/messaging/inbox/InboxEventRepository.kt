package com.mnemos.messaging.inbox

import java.time.Instant
import java.util.UUID

interface InboxEventRepository {

    fun insertIfAbsent(
        consumer: String,
        eventId: UUID,
        recordedAt: Instant,
    ): Boolean
}