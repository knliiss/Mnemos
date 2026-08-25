package com.mnemos.personalization.application.personalization.builtin

import com.mnemos.personalization.domain.personalization.model.PersonalizationCatalogKey
import com.mnemos.personalization.domain.personalization.model.PersonalizationContent
import com.mnemos.personalization.domain.personalization.model.PersonalizationPrice
import com.mnemos.personalization.domain.personalization.model.PersonalizationStatus
import com.mnemos.personalization.domain.personalization.model.PersonalizationType

data class BuiltinPersonalizationDefinition(
    val catalogKey: PersonalizationCatalogKey,
    val type: PersonalizationType,
    val name: String,
    val content: PersonalizationContent,
    val price: PersonalizationPrice,
    val status: PersonalizationStatus = PersonalizationStatus.PUBLISHED,
)
