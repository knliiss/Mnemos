package com.mnemos.personalization.application.personalization.usecase.reject

import com.mnemos.personalization.application.personalization.support.PersonalizationModerationSupport
import com.mnemos.personalization.domain.personalization.model.Personalization
import com.mnemos.personalization.domain.personalization.model.PersonalizationId
import com.mnemos.personalization.domain.personalization.repository.PersonalizationRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Clock

@Service
class RejectPersonalizationService(
    private val support: PersonalizationModerationSupport,
    private val repository: PersonalizationRepository,
    private val clock: Clock,
) : RejectPersonalizationUseCase {
    @Transactional
    override fun execute(
        personalizationId: PersonalizationId,
        reason: String,
    ): Personalization {
        val actor = support.requireModerator()
        val personalization = support.requirePersonalizationForUpdate(personalizationId)

        support.requireCanModerate(actor, personalization)
        personalization.reject(
            reason = reason,
            now = clock.instant(),
        )

        return repository.save(personalization)
    }
}
