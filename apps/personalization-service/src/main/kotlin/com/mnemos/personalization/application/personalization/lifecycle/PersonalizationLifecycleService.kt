package com.mnemos.personalization.application.personalization.lifecycle

import com.mnemos.common.core.model.id.UserId
import com.mnemos.personalization.application.personalization.exception.PersonalizationAccessDeniedException
import com.mnemos.personalization.application.personalization.exception.PersonalizationNotFoundException
import com.mnemos.personalization.application.pricing.PersonalizationPricePolicy
import com.mnemos.personalization.application.security.port.out.CurrentPersonalizationActorProvider
import com.mnemos.personalization.domain.personalization.model.Personalization
import com.mnemos.personalization.domain.personalization.model.PersonalizationContent
import com.mnemos.personalization.domain.personalization.model.PersonalizationId
import com.mnemos.personalization.domain.personalization.model.PersonalizationOrigin
import com.mnemos.personalization.domain.personalization.model.PersonalizationPrice
import com.mnemos.personalization.domain.personalization.repository.PersonalizationRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Clock

data class UpdatePersonalizationCommand(
    val personalizationId: PersonalizationId,
    val name: String,
    val content: PersonalizationContent,
    val price: PersonalizationPrice?,
)

@Service
class PersonalizationLifecycleService(
    private val actorProvider: CurrentPersonalizationActorProvider,
    private val pricePolicy: PersonalizationPricePolicy,
    private val repository: PersonalizationRepository,
    private val clock: Clock,
) {
    @Transactional
    fun update(command: UpdatePersonalizationCommand): Personalization {
        val actor = actorProvider.requireActor()
        val personalization = requireOwned(command.personalizationId, actor.userId)

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

    @Transactional
    fun submit(personalizationId: PersonalizationId): Personalization {
        val actor = actorProvider.requireActor()
        val personalization = requireOwned(personalizationId, actor.userId)

        if (personalization.origin == PersonalizationOrigin.CUSTOM &&
            !actor.canCreateMarketplacePersonalization()
        ) {
            throw PersonalizationAccessDeniedException()
        }

        personalization.submitForReview(clock.instant())

        return repository.save(personalization)
    }

    @Transactional
    fun publish(personalizationId: PersonalizationId): Personalization {
        val actor = actorProvider.requireActor()
        val personalization = requireOwned(personalizationId, actor.userId)

        personalization.publish(clock.instant())

        return repository.save(personalization)
    }

    fun listMine(): List<Personalization> {
        val actor = actorProvider.requireActor()

        return repository.findByCreatorUserId(actor.userId)
    }

    private fun requireOwned(
        personalizationId: PersonalizationId,
        userId: UserId,
    ): Personalization {
        val personalization =
            repository.findById(personalizationId)
                ?: throw PersonalizationNotFoundException()

        if (personalization.creatorUserId != userId) {
            throw PersonalizationAccessDeniedException()
        }

        return personalization
    }
}
