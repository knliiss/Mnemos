package com.mnemos.personalization.application.personalization.usecase.listreviewqueue

import com.mnemos.personalization.application.personalization.support.PersonalizationModerationSupport
import com.mnemos.personalization.domain.personalization.model.Personalization
import com.mnemos.personalization.domain.personalization.model.PersonalizationStatus
import com.mnemos.personalization.domain.personalization.repository.PersonalizationRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class ListPersonalizationReviewQueueService(
    private val support: PersonalizationModerationSupport,
    private val repository: PersonalizationRepository,
) : ListPersonalizationReviewQueueUseCase {
    @Transactional(readOnly = true)
    override fun execute(): List<Personalization> {
        support.requireModerator()

        return repository.findByStatus(PersonalizationStatus.IN_REVIEW)
    }
}
