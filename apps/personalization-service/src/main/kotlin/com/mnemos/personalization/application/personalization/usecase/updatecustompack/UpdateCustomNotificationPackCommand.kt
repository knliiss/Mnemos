package com.mnemos.personalization.application.personalization.usecase.updatecustompack

import com.mnemos.personalization.domain.personalization.model.PersonalizationContent
import com.mnemos.personalization.domain.personalization.model.PersonalizationId

data class UpdateCustomNotificationPackCommand(
    val personalizationId: PersonalizationId,
    val name: String,
    val content: PersonalizationContent.NotificationPack,
)
