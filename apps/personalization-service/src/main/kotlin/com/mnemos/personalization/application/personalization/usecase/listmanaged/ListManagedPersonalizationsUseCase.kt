package com.mnemos.personalization.application.personalization.usecase.listmanaged

import com.mnemos.personalization.domain.personalization.model.Personalization

fun interface ListManagedPersonalizationsUseCase {
    fun execute(): List<Personalization>
}
