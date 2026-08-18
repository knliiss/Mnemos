package com.mnemos.social.adapter.out.http.personalization

import com.mnemos.social.application.spotlight.port.out.NotificationPackSnapshotProvider
import com.mnemos.social.domain.spotlight.model.NotificationPackSelectionSnapshot
import org.springframework.stereotype.Component
import tools.jackson.databind.ObjectMapper
import java.util.UUID

@Component
class NotificationPackSnapshotHttpAdapter(
    private val client: PersonalizationServiceClient,
    private val objectMapper: ObjectMapper,
) : NotificationPackSnapshotProvider {
    override fun findActive(userId: UUID): NotificationPackSelectionSnapshot? {
        val selection = client.getNotificationPackSelection(userId) ?: return null

        return NotificationPackSelectionSnapshot(
            personalizationId = selection.personalizationId,
            revision = selection.selectionRevision,
            snapshotJson = objectMapper.writeValueAsString(selection.pack),
        )
    }
}
