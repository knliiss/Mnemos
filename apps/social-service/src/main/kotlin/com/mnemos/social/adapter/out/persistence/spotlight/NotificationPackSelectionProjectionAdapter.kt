package com.mnemos.social.adapter.out.persistence.spotlight

import com.fasterxml.jackson.databind.ObjectMapper
import com.mnemos.personalization.contracts.event.NotificationPackSnapshotV1
import com.mnemos.social.application.spotlight.port.out.NotificationPackSelectionProjection
import org.springframework.stereotype.Component
import java.time.Instant
import java.util.UUID

@Component
class NotificationPackSelectionProjectionAdapter(
    private val repository: NotificationPackSelectionJpaRepository,
    private val objectMapper: ObjectMapper,
) : NotificationPackSelectionProjection {
    override fun apply(
        userId: UUID,
        personalizationId: UUID?,
        pack: NotificationPackSnapshotV1?,
        revision: Long,
        occurredAt: Instant,
    ) {
        val current = repository.findById(userId).orElse(null)
        if (current != null && current.selectionRevision >= revision) {
            return
        }

        repository.save(
            NotificationPackSelectionEntity(
                userId = userId,
                personalizationId = personalizationId,
                selectionRevision = revision,
                snapshotJson = pack?.let(objectMapper::writeValueAsString),
                occurredAt = occurredAt,
            ),
        )
    }
}
