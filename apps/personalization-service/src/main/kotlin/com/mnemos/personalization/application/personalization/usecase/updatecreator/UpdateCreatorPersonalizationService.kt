package com.mnemos.personalization.application.personalization.usecase.updatecreator

import com.mnemos.personalization.application.personalization.exception.PersonalizationAccessDeniedException
import com.mnemos.personalization.application.personalization.support.PersonalizationCreatorSupport
import com.mnemos.personalization.application.pricing.PersonalizationPricePolicy
import com.mnemos.personalization.domain.personalization.model.Personalization
import com.mnemos.personalization.domain.personalization.model.PersonalizationOrigin
import com.mnemos.personalization.domain.personalization.repository.PersonalizationRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Clock

@Service
class UpdateCreatorPersonalizationService(
    private val support: PersonalizationCreatorSupport,
    private val pricePolicy: PersonalizationPricePolicy,
    private val repository: PersonalizationRepository,
    private val clock: Clock,
) : UpdateCreatorPersonalizationUseCase {
    @Transactional
    override fun execute(command: UpdatePersonalizationCommand): Personalization {
        val actor = support.requireMarketplaceActor()
        val personalization = support.requireOwnedForUpdate(command.personalizationId, actor.userId)

        if (personalization.origin != PersonalizationOrigin.CREATOR) {
            throw PersonalizationAccessDeniedException()
        }

        command.price?.let { price ->
            pricePolicy.requireAllowed(
                type = personalization.type,
                origin = personalization.origin,
                price = price,
            )
        }

        personalization.updateDraft(
            name = command.name,
            content = command.content,
            price = command.price,
            now = clock.instant(),
        )

        return repository.save(personalization)
    }
}
