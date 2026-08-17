package com.mnemos.personalization.application.personalization.usecase.createcustompack

import com.mnemos.personalization.domain.personalization.model.Personalization

data class CreateCustomNotificationPackResult(
    val personalization: Personalization,
    val grantedForFree: Boolean,
)
