package com.mnemos.personalization.contracts.internal.v1

import com.mnemos.personalization.contracts.event.NotificationPackSnapshotV1
import java.util.UUID

data class InternalTelegramNotificationPackSelectionResponse(
    val personalizationId: UUID,
    val name: String,
    val selectionRevision: Long,
    val pack: NotificationPackSnapshotV1,
)
