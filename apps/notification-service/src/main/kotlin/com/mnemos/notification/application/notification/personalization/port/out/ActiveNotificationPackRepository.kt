package com.mnemos.notification.application.notification.personalization.port.out

import com.mnemos.common.core.model.id.UserId
import com.mnemos.notification.application.notification.personalization.model.ActiveNotificationPack
import java.time.Instant
import java.util.UUID

interface ActiveNotificationPackRepository {
    fun find(userId: UserId): ActiveNotificationPack?

    fun apply(
        userId: UserId,
        pack: ActiveNotificationPack?,
        selectionRevision: Long,
        eventId: UUID,
        occurredAt: Instant,
    )
}
