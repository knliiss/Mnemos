package com.mnemos.personalization.application.personalization.usecase.unlist

import com.mnemos.personalization.application.personalization.support.PersonalizationModerationSupport
import com.mnemos.personalization.domain.personalization.model.Personalization
import com.mnemos.personalization.domain.personalization.model.PersonalizationId
import com.mnemos.personalization.domain.personalization.repository.PersonalizationRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Clock

@Service
class UnlistPersonalizationService(
    private val support: PersonalizationModerationSupport,
    private val repository: PersonalizationRepository,
    private val clock: Clock,
) : UnlistPersonalizationUseCase {
    @Transactional
    override fun execute(personalizationId: PersonalizationId): Personalization {
        val actor = support.requireModerator()
        val personalization = support.requirePersonalizationForUpdate(personalizationId)

        support.requireCanModerate(actor, personalization)
        personalization.unlist(clock.instant())

        return repository.save(personalization)
    }
}
