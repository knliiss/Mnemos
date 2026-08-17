package com.mnemos.personalization.application.personalization.usecase.getcatalog

import com.mnemos.personalization.application.personalization.exception.PersonalizationNotFoundException
import com.mnemos.personalization.domain.personalization.model.Personalization
import com.mnemos.personalization.domain.personalization.model.PersonalizationId
import com.mnemos.personalization.domain.personalization.model.PersonalizationStatus
import com.mnemos.personalization.domain.personalization.model.PersonalizationVisibility
import com.mnemos.personalization.domain.personalization.repository.PersonalizationRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class GetPersonalizationService(
    private val repository: PersonalizationRepository,
) : GetPersonalizationUseCase {
    @Transactional(readOnly = true)
    override fun execute(personalizationId: PersonalizationId): Personalization {
        val personalization =
            repository.findById(personalizationId)
                ?: throw PersonalizationNotFoundException()
        val visible =
            personalization.status == PersonalizationStatus.PUBLISHED &&
                personalization.visibility == PersonalizationVisibility.PUBLIC

        if (!visible) {
            throw PersonalizationNotFoundException()
        }

        return personalization
    }
}
