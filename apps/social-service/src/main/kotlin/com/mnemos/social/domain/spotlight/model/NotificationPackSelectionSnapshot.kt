package com.mnemos.social.domain.spotlight.model

import java.util.UUID

data class NotificationPackSelectionSnapshot(
    val personalizationId: UUID,
    val revision: Long,
    val snapshotJson: String,
)
