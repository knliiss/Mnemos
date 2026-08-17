package com.mnemos.personalization.application.personalization.usecase.updatecreator

import com.mnemos.personalization.domain.personalization.model.PersonalizationContent
import com.mnemos.personalization.domain.personalization.model.PersonalizationId
import com.mnemos.personalization.domain.personalization.model.PersonalizationPrice

data class UpdatePersonalizationCommand(
    val personalizationId: PersonalizationId,
    val name: String,
    val content: PersonalizationContent,
    val price: PersonalizationPrice?,
)
