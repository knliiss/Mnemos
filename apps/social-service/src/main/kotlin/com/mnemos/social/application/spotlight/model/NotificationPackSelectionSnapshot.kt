package com.mnemos.social.application.spotlight.model

import com.mnemos.personalization.contracts.event.NotificationPackSnapshotV1
import java.util.UUID

data class NotificationPackSelectionSnapshot(
    val personalizationId: UUID,
    val revision: Long,
    val pack: NotificationPackSnapshotV1,
)
