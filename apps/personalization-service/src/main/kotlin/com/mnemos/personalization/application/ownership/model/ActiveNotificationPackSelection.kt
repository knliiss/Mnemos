package com.mnemos.personalization.application.ownership.model

import com.mnemos.common.core.model.id.UserId
import com.mnemos.personalization.domain.personalization.model.PersonalizationContent
import com.mnemos.personalization.domain.personalization.model.PersonalizationId

data class ActiveNotificationPackSelection(
    val userId: UserId,
    val personalizationId: PersonalizationId,
    val selectionRevision: Long,
    val content: PersonalizationContent.NotificationPack,
)
