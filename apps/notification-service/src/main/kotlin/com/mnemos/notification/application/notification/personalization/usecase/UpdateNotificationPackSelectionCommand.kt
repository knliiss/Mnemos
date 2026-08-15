package com.mnemos.notification.application.notification.personalization.usecase

import com.mnemos.common.core.model.id.UserId
import com.mnemos.notification.application.notification.personalization.model.ActiveNotificationPack
import java.time.Instant
import java.util.UUID

data class UpdateNotificationPackSelectionCommand(
    val userId: UserId,
    val pack: ActiveNotificationPack?,
    val eventId: UUID,
    val occurredAt: Instant,
)
