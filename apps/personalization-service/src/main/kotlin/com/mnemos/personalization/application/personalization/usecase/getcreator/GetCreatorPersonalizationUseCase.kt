package com.mnemos.personalization.application.personalization.usecase.getcreator

import com.mnemos.personalization.domain.personalization.model.Personalization
import com.mnemos.personalization.domain.personalization.model.PersonalizationId

fun interface GetCreatorPersonalizationUseCase {
    fun execute(personalizationId: PersonalizationId): Personalization
}
