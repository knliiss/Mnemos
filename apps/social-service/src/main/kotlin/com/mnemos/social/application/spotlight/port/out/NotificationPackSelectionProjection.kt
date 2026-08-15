package com.mnemos.social.application.spotlight.port.out

import java.time.Instant
import java.util.UUID

interface NotificationPackSelectionProjection {
    fun apply(
        userId: UUID,
        personalizationId: UUID?,
        snapshotJson: String?,
        revision: Long,
        occurredAt: Instant,
    )
}
