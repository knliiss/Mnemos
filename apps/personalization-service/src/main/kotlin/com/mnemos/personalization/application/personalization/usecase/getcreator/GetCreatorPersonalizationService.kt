package com.mnemos.personalization.application.personalization.usecase.getcreator

import com.mnemos.personalization.application.personalization.exception.PersonalizationNotFoundException
import com.mnemos.personalization.application.personalization.support.PersonalizationCreatorSupport
import com.mnemos.personalization.domain.personalization.model.Personalization
import com.mnemos.personalization.domain.personalization.model.PersonalizationId
import com.mnemos.personalization.domain.personalization.repository.PersonalizationRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class GetCreatorPersonalizationService(
    private val support: PersonalizationCreatorSupport,
    private val repository: PersonalizationRepository,
) : GetCreatorPersonalizationUseCase {
    @Transactional(readOnly = true)
    override fun execute(personalizationId: PersonalizationId): Personalization {
        val actor = support.requireActor()
        val personalization = repository.findById(personalizationId) ?: throw PersonalizationNotFoundException()

        if (personalization.creatorUserId != actor.userId) {
            throw PersonalizationNotFoundException()
        }

        return personalization
    }
}
