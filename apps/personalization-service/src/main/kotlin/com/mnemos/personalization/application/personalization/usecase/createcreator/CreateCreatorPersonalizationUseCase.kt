package com.mnemos.personalization.application.personalization.usecase.createcreator

import com.mnemos.personalization.domain.personalization.model.Personalization

interface CreateCreatorPersonalizationUseCase {
    fun execute(command: CreateCreatorPersonalizationCommand): Personalization
}
