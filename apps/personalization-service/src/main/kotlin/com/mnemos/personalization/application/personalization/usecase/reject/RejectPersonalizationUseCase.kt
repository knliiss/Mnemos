package com.mnemos.personalization.application.personalization.usecase.reject

import com.mnemos.personalization.domain.personalization.model.Personalization
import com.mnemos.personalization.domain.personalization.model.PersonalizationId

interface RejectPersonalizationUseCase {
    fun execute(
        personalizationId: PersonalizationId,
        reason: String,
    ): Personalization
}
