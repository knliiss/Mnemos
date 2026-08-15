package com.mnemos.social.application.spotlight.port.out

import com.mnemos.personalization.contracts.event.NotificationPackSnapshotV1
import java.time.Instant
import java.util.UUID

interface NotificationPackSelectionProjection {
    fun apply(
        userId: UUID,
        personalizationId: UUID?,
        pack: NotificationPackSnapshotV1?,
        revision: Long,
        occurredAt: Instant,
    )
}
