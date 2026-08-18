package com.mnemos.personalization.application.personalization.usecase.relist

import com.mnemos.personalization.domain.personalization.model.Personalization
import com.mnemos.personalization.domain.personalization.model.PersonalizationId

interface RelistPersonalizationUseCase {
    fun execute(personalizationId: PersonalizationId): Personalization
}
