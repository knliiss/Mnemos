package com.mnemos.social.application.spotlight

import com.mnemos.social.application.spotlight.port.out.NotificationPackSelectionProjection
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import java.util.UUID

@Service
class UpdateNotificationPackSelectionService(
    private val projection: NotificationPackSelectionProjection,
) {
    @Transactional
    fun execute(
        userId: UUID,
        personalizationId: UUID?,
        snapshotJson: String?,
        revision: Long,
        occurredAt: Instant,
    ) {
        projection.apply(
            userId = userId,
            personalizationId = personalizationId,
            snapshotJson = snapshotJson,
            revision = revision,
            occurredAt = occurredAt,
        )
    }
}
