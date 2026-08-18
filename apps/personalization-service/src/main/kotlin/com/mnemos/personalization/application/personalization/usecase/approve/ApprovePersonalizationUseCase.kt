package com.mnemos.personalization.application.personalization.usecase.approve

import com.mnemos.personalization.domain.personalization.model.Personalization
import com.mnemos.personalization.domain.personalization.model.PersonalizationId
import com.mnemos.personalization.domain.personalization.model.PersonalizationPrice

interface ApprovePersonalizationUseCase {
    fun execute(
        personalizationId: PersonalizationId,
        price: PersonalizationPrice,
    ): Personalization
}
