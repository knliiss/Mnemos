package com.mnemos.social.adapter.out.persistence.spotlight

import com.mnemos.social.application.spotlight.port.out.NotificationPackSnapshotProvider
import com.mnemos.social.domain.spotlight.model.NotificationPackSelectionSnapshot
import org.springframework.stereotype.Component
import java.util.UUID

@Component
class NotificationPackSnapshotAdapter(
    private val repository: NotificationPackSelectionJpaRepository,
) : NotificationPackSnapshotProvider {
    override fun findActive(userId: UUID): NotificationPackSelectionSnapshot? {
        val selection = repository.findById(userId).orElse(null) ?: return null
        val personalizationId = selection.personalizationId ?: return null
        val snapshotJson = selection.snapshotJson ?: return null

        return NotificationPackSelectionSnapshot(
            personalizationId = personalizationId,
            revision = selection.selectionRevision,
            snapshotJson = snapshotJson,
        )
    }
}
