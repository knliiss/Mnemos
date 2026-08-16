package com.mnemos.personalization.application.ownership.usecase.deactivate

import com.mnemos.personalization.domain.ownership.model.OwnedPersonalization
import com.mnemos.personalization.domain.personalization.model.PersonalizationId

interface DeactivatePersonalizationUseCase {
    fun execute(personalizationId: PersonalizationId): OwnedPersonalization
}
