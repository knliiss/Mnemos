package com.mnemos.personalization.application.personalization.usecase.relist

import com.mnemos.personalization.application.personalization.support.PersonalizationModerationSupport
import com.mnemos.personalization.domain.personalization.model.Personalization
import com.mnemos.personalization.domain.personalization.model.PersonalizationId
import com.mnemos.personalization.domain.personalization.repository.PersonalizationRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Clock

@Service
class RelistPersonalizationService(
    private val support: PersonalizationModerationSupport,
    private val repository: PersonalizationRepository,
    private val clock: Clock,
) : RelistPersonalizationUseCase {
    @Transactional
    override fun execute(personalizationId: PersonalizationId): Personalization {
        val actor = support.requireModerator()
        val personalization = support.requirePersonalizationForUpdate(personalizationId)

        support.requireCanModerate(actor, personalization)
        personalization.relist(clock.instant())

        return repository.save(personalization)
    }
}
