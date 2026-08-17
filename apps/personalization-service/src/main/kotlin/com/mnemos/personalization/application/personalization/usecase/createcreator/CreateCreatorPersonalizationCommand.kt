package com.mnemos.personalization.application.personalization.usecase.createcreator

import com.mnemos.personalization.domain.personalization.model.PersonalizationContent
import com.mnemos.personalization.domain.personalization.model.PersonalizationPrice
import com.mnemos.personalization.domain.personalization.model.PersonalizationType

data class CreateCreatorPersonalizationCommand(
    val type: PersonalizationType,
    val name: String,
    val content: PersonalizationContent,
    val price: PersonalizationPrice,
)
