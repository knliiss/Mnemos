package com.mnemos.personalization.application.personalization.usecase.createcustompack

import com.mnemos.personalization.domain.personalization.model.PersonalizationContent

data class CreateCustomNotificationPackCommand(
    val name: String,
    val content: PersonalizationContent.NotificationPack,
)
