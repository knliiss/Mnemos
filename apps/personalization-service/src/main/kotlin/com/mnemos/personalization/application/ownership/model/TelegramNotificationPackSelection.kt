package com.mnemos.personalization.application.ownership.model

import com.mnemos.personalization.domain.personalization.model.PersonalizationContent
import com.mnemos.personalization.domain.personalization.model.PersonalizationId

data class TelegramNotificationPackSelection(
    val personalizationId: PersonalizationId,
    val name: String,
    val selectionRevision: Long,
    val content: PersonalizationContent.NotificationPack,
)
