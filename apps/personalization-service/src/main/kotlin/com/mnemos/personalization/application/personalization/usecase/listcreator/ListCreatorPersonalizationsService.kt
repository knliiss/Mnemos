package com.mnemos.personalization.application.personalization.usecase.listcreator

import com.mnemos.personalization.application.personalization.support.PersonalizationCreatorSupport
import com.mnemos.personalization.domain.personalization.model.Personalization
import com.mnemos.personalization.domain.personalization.repository.PersonalizationRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class ListCreatorPersonalizationsService(
    private val support: PersonalizationCreatorSupport,
    private val repository: PersonalizationRepository,
) : ListCreatorPersonalizationsUseCase {
    @Transactional(readOnly = true)
    override fun execute(): List<Personalization> {
        val actor = support.requireActor()

        return repository.findByCreatorUserId(actor.userId)
    }
}
