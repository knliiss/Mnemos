package com.mnemos.personalization.application.personalization.usecase.listcreator

import com.mnemos.personalization.domain.personalization.model.Personalization

interface ListCreatorPersonalizationsUseCase {
    fun execute(): List<Personalization>
}
