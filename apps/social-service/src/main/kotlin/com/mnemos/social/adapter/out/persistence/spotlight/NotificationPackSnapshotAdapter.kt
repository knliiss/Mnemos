package com.mnemos.social.adapter.out.persistence.spotlight

import com.fasterxml.jackson.databind.ObjectMapper
import com.mnemos.personalization.contracts.event.NotificationPackSnapshotV1
import com.mnemos.social.application.spotlight.model.NotificationPackSelectionSnapshot
import com.mnemos.social.application.spotlight.port.out.NotificationPackSnapshotProvider
import org.springframework.stereotype.Component
import java.util.UUID

@Component
class NotificationPackSnapshotAdapter(
    private val repository: NotificationPackSelectionJpaRepository,
    private val objectMapper: ObjectMapper,
) : NotificationPackSnapshotProvider {
    override fun findActive(userId: UUID): NotificationPackSelectionSnapshot? {
        val selection = repository.findById(userId).orElse(null) ?: return null
        val personalizationId = selection.personalizationId ?: return null
        val snapshotJson = selection.snapshotJson ?: return null

        return NotificationPackSelectionSnapshot(
            personalizationId = personalizationId,
            revision = selection.selectionRevision,
            pack = objectMapper.readValue(snapshotJson, NotificationPackSnapshotV1::class.java),
        )
    }
}
