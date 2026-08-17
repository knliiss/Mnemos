package com.mnemos.personalization.application.ownership.usecase.activate

import com.mnemos.personalization.domain.ownership.model.OwnedPersonalization
import com.mnemos.personalization.domain.personalization.model.PersonalizationId

interface ActivatePersonalizationUseCase {
    fun execute(personalizationId: PersonalizationId): OwnedPersonalization
}
