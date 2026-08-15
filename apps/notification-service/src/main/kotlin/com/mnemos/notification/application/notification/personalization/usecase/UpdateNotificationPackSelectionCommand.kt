package com.mnemos.notification.application.notification.personalization.usecase

import com.mnemos.common.core.model.id.UserId
import com.mnemos.notification.application.notification.personalization.model.ActiveNotificationPack

data class UpdateNotificationPackSelectionCommand(
    val userId: UserId,
    val pack: ActiveNotificationPack?,
)
