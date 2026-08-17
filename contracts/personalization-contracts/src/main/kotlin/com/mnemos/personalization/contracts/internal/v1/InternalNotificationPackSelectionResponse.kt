package com.mnemos.personalization.contracts.internal.v1

import com.mnemos.personalization.contracts.event.NotificationPackSnapshotV1
import java.util.UUID

data class InternalNotificationPackSelectionResponse(
    val userId: UUID,
    val personalizationId: UUID,
    val selectionRevision: Long,
    val pack: NotificationPackSnapshotV1,
)
