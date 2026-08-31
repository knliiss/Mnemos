package com.mnemos.personalization.application.ownership.model

import com.mnemos.personalization.domain.personalization.model.PersonalizationId

data class TelegramNotificationPackOption(
    val personalizationId: PersonalizationId,
    val name: String,
    val active: Boolean,
)
