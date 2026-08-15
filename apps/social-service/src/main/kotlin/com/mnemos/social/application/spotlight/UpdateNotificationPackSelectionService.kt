package com.mnemos.social.application.spotlight

import com.mnemos.personalization.contracts.event.NotificationPackSelectionChangedV1
import com.mnemos.social.application.spotlight.port.out.NotificationPackSelectionProjection
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class UpdateNotificationPackSelectionService(
    private val projection: NotificationPackSelectionProjection,
) {
    @Transactional
    fun execute(event: NotificationPackSelectionChangedV1) {
        projection.apply(
            userId = event.userId,
            personalizationId = event.personalizationId,
            pack = event.pack,
            revision = event.selectionRevision,
            occurredAt = event.occurredAt,
        )
    }
}
