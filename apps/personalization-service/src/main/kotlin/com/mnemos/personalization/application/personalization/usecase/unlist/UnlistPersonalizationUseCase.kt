package com.mnemos.personalization.application.personalization.usecase.unlist

import com.mnemos.personalization.domain.personalization.model.Personalization
import com.mnemos.personalization.domain.personalization.model.PersonalizationId

interface UnlistPersonalizationUseCase {
    fun execute(personalizationId: PersonalizationId): Personalization
}
