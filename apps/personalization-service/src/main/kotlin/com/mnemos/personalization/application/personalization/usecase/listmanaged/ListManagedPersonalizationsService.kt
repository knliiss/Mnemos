package com.mnemos.personalization.application.personalization.usecase.listmanaged

import com.mnemos.personalization.application.personalization.support.PersonalizationModerationSupport
import com.mnemos.personalization.domain.personalization.model.Personalization
import com.mnemos.personalization.domain.personalization.model.PersonalizationStatus
import com.mnemos.personalization.domain.personalization.repository.PersonalizationRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class ListManagedPersonalizationsService(
    private val support: PersonalizationModerationSupport,
    private val repository: PersonalizationRepository,
) : ListManagedPersonalizationsUseCase {
    @Transactional(readOnly = true)
    override fun execute(): List<Personalization> {
        support.requireModerator()

        return (
            repository.findByStatus(PersonalizationStatus.PUBLISHED) +
                repository.findByStatus(PersonalizationStatus.UNLISTED)
        ).sortedByDescending(Personalization::updatedAt)
    }
}
