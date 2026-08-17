package com.mnemos.personalization.application.personalization.usecase.getcatalog

import com.mnemos.personalization.domain.personalization.model.Personalization
import com.mnemos.personalization.domain.personalization.model.PersonalizationId

interface GetPersonalizationUseCase {
    fun execute(personalizationId: PersonalizationId): Personalization
}
