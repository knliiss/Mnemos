package com.mnemos.personalization.application.personalization.usecase.approve

import com.mnemos.personalization.application.personalization.support.PersonalizationModerationSupport
import com.mnemos.personalization.application.pricing.PersonalizationPricePolicy
import com.mnemos.personalization.domain.personalization.model.Personalization
import com.mnemos.personalization.domain.personalization.model.PersonalizationId
import com.mnemos.personalization.domain.personalization.model.PersonalizationPrice
import com.mnemos.personalization.domain.personalization.repository.PersonalizationRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Clock

@Service
class ApprovePersonalizationService(
    private val support: PersonalizationModerationSupport,
    private val pricePolicy: PersonalizationPricePolicy,
    private val repository: PersonalizationRepository,
    private val clock: Clock,
) : ApprovePersonalizationUseCase {
    @Transactional
    override fun execute(
        personalizationId: PersonalizationId,
        price: PersonalizationPrice,
    ): Personalization {
        val actor = support.requireModerator()
        val personalization = support.requirePersonalizationForUpdate(personalizationId)

        support.requireCanModerate(actor, personalization)
        pricePolicy.requireAllowed(
            type = personalization.type,
            origin = personalization.origin,
            price = price,
        )

        val now = clock.instant()

        personalization.approve(
            moderatedPrice = price,
            now = now,
        )
        personalization.publish(now)

        return repository.save(personalization)
    }
}
