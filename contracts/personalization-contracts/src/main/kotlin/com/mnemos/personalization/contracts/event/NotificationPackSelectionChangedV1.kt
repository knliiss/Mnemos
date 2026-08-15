package com.mnemos.personalization.contracts.event

import java.time.Instant
import java.util.UUID

data class NotificationPackSelectionChangedV1(
    val userId: UUID,
    val personalizationId: UUID?,
    val pack: NotificationPackSnapshotV1?,
    val selectionRevision: Long,
    val occurredAt: Instant,
) {
    init {
        require((personalizationId == null) == (pack == null)) {
            "Notification pack selection must contain both personalizationId and pack or neither"
        }
        require(selectionRevision > 0) {
            "Notification pack selection revision must be positive"
        }
    }
}
