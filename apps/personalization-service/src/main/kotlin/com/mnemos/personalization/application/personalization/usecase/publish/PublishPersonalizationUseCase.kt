package com.mnemos.personalization.application.personalization.usecase.publish

import com.mnemos.personalization.domain.personalization.model.Personalization
import com.mnemos.personalization.domain.personalization.model.PersonalizationId

interface PublishPersonalizationUseCase {
    fun execute(personalizationId: PersonalizationId): Personalization
}
