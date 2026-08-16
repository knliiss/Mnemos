package com.mnemos.personalization.application.personalization.usecase.createcreator

import com.mnemos.personalization.application.personalization.exception.PersonalizationAccessDeniedException
import com.mnemos.personalization.application.pricing.PersonalizationPricePolicy
import com.mnemos.personalization.application.security.port.out.CurrentPersonalizationActorProvider
import com.mnemos.personalization.domain.personalization.model.Personalization
import com.mnemos.personalization.domain.personalization.model.PersonalizationOrigin
import com.mnemos.personalization.domain.personalization.repository.PersonalizationRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Clock

@Service
class CreateCreatorPersonalizationService(
    private val actorProvider: CurrentPersonalizationActorProvider,
    private val pricePolicy: PersonalizationPricePolicy,
    private val repository: PersonalizationRepository,
    private val clock: Clock,
) : CreateCreatorPersonalizationUseCase {
    @Transactional
    override fun execute(command: CreateCreatorPersonalizationCommand): Personalization {
        val actor = actorProvider.requireActor()

        if (!actor.canCreateMarketplacePersonalization()) {
            throw PersonalizationAccessDeniedException()
        }

        pricePolicy.requireAllowed(
            type = command.type,
            origin = PersonalizationOrigin.CREATOR,
            price = command.price,
        )

        val personalization =
            Personalization.createCreatorDraft(
                creatorUserId = actor.userId,
                type = command.type,
                name = command.name,
                content = command.content,
                price = command.price,
                now = clock.instant(),
            )

        return repository.save(personalization)
    }
}
