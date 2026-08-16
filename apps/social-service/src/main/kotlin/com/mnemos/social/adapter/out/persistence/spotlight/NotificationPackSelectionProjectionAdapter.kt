package com.mnemos.social.adapter.out.persistence.spotlight

import com.mnemos.social.application.spotlight.port.out.NotificationPackSelectionProjection
import org.springframework.stereotype.Component
import java.time.Instant
import java.util.UUID

@Component
class NotificationPackSelectionProjectionAdapter(
    private val repository: NotificationPackSelectionJpaRepository,
) : NotificationPackSelectionProjection {
    override fun apply(
        userId: UUID,
        personalizationId: UUID?,
        snapshotJson: String?,
        revision: Long,
        occurredAt: Instant,
    ) {
        repository.applyIfNewer(
            userId = userId,
            personalizationId = personalizationId,
            revision = revision,
            snapshotJson = snapshotJson,
            occurredAt = occurredAt,
        )
    }
}
