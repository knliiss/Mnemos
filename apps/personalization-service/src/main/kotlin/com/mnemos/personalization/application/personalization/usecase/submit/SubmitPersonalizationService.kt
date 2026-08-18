package com.mnemos.personalization.application.personalization.usecase.submit

import com.mnemos.personalization.application.personalization.support.PersonalizationCreatorSupport
import com.mnemos.personalization.domain.personalization.model.Personalization
import com.mnemos.personalization.domain.personalization.model.PersonalizationId
import com.mnemos.personalization.domain.personalization.repository.PersonalizationRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Clock

@Service
class SubmitPersonalizationService(
    private val support: PersonalizationCreatorSupport,
    private val repository: PersonalizationRepository,
    private val clock: Clock,
) : SubmitPersonalizationUseCase {
    @Transactional
    override fun execute(personalizationId: PersonalizationId): Personalization {
        val actor = support.requireMarketplaceActor()
        val personalization = support.requireOwnedForUpdate(personalizationId, actor.userId)

        support.requireCustomPackOwnershipBeforeSubmission(
            personalization = personalization,
            userId = actor.userId,
        )
        personalization.submitForReview(clock.instant())

        return repository.save(personalization)
    }
}
