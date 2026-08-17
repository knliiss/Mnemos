package com.mnemos.personalization.application.personalization.usecase.submit

import com.mnemos.personalization.domain.personalization.model.Personalization
import com.mnemos.personalization.domain.personalization.model.PersonalizationId

interface SubmitPersonalizationUseCase {
    fun execute(personalizationId: PersonalizationId): Personalization
}
