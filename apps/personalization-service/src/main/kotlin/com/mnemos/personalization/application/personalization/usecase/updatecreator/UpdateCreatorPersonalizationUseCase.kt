package com.mnemos.personalization.application.personalization.usecase.updatecreator

import com.mnemos.personalization.domain.personalization.model.Personalization

interface UpdateCreatorPersonalizationUseCase {
    fun execute(command: UpdatePersonalizationCommand): Personalization
}
