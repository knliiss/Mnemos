package com.mnemos.personalization.application.personalization.lifecycle

import com.mnemos.common.core.model.id.UserId
import com.mnemos.personalization.application.ownership.event.NotificationPackSelectionEventPublisher
import com.mnemos.personalization.application.ownership.exception.PersonalizationNotOwnedException
import com.mnemos.personalization.application.personalization.exception.PersonalizationAccessDeniedException
import com.mnemos.personalization.application.personalization.exception.PersonalizationNotFoundException
import com.mnemos.personalization.application.pricing.PersonalizationPricePolicy
import com.mnemos.personalization.application.security.model.PersonalizationActor
import com.mnemos.personalization.application.security.port.out.CurrentPersonalizationActorProvider
import com.mnemos.personalization.domain.ownership.model.PersonalizationOwner
import com.mnemos.personalization.domain.ownership.repository.OwnedPersonalizationRepository
import com.mnemos.personalization.domain.personalization.model.Personalization
import com.mnemos.personalization.domain.personalization.model.PersonalizationContent
import com.mnemos.personalization.domain.personalization.model.PersonalizationId
import com.mnemos.personalization.domain.personalization.model.PersonalizationOrigin
import com.mnemos.personalization.domain.personalization.model.PersonalizationPrice
import com.mnemos.personalization.domain.personalization.model.PersonalizationType
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
    private val ownershipRepository: OwnedPersonalizationRepository,
    private val notificationPackSelectionEventPublisher: NotificationPackSelectionEventPublisher,
    private val clock: Clock,
) {
    @Transactional
    fun update(command: UpdatePersonalizationCommand): Personalization {
        val actor = actorProvider.requireActor()

        requireMarketplaceCreator(actor)

        val personalization = requireOwnedForUpdate(command.personalizationId, actor.userId)

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

    @Transactional
    fun updateCustomNotificationPack(
        personalizationId: PersonalizationId,
        name: String,
        content: PersonalizationContent.NotificationPack,
    ): Personalization {
        val actor = actorProvider.requireActor()
        val personalization = requireOwnedForUpdate(personalizationId, actor.userId)

        if (personalization.origin != PersonalizationOrigin.CUSTOM ||
            personalization.type != PersonalizationType.NOTIFICATION_PACK
        ) {
            throw PersonalizationAccessDeniedException()
        }

        personalization.updateDraft(
            name = name,
            content = content,
            price = personalization.price,
            now = clock.instant(),
        )

        val saved = repository.save(personalization)

        republishActiveNotificationPack(
            personalization = saved,
            userId = actor.userId,
        )

        return saved
    }

    @Transactional
    fun submit(personalizationId: PersonalizationId): Personalization {
        val actor = actorProvider.requireActor()

        requireMarketplaceCreator(actor)

        val personalization = requireOwnedForUpdate(personalizationId, actor.userId)

        requireCustomPackOwnershipBeforeSubmission(
            personalization = personalization,
            userId = actor.userId,
        )

        personalization.submitForReview(clock.instant())

        return repository.save(personalization)
    }

    @Transactional
    fun publish(personalizationId: PersonalizationId): Personalization {
        val actor = actorProvider.requireActor()

        requireMarketplaceCreator(actor)

        val personalization = requireOwnedForUpdate(personalizationId, actor.userId)

        personalization.publish(clock.instant())

        return repository.save(personalization)
    }

    fun listMine(): List<Personalization> {
        val actor = actorProvider.requireActor()

        return repository.findByCreatorUserId(actor.userId)
    }

    private fun requireMarketplaceCreator(actor: PersonalizationActor) {
        if (!actor.canCreateMarketplacePersonalization()) {
            throw PersonalizationAccessDeniedException()
        }
    }

    private fun requireCustomPackOwnershipBeforeSubmission(
        personalization: Personalization,
        userId: UserId,
    ) {
        if (personalization.origin != PersonalizationOrigin.CUSTOM) {
            return
        }

        val owned =
            ownershipRepository.exists(
                owner = PersonalizationOwner.User(userId),
                personalizationId = personalization.id,
            )

        if (!owned) {
            throw PersonalizationNotOwnedException()
        }
    }

    private fun republishActiveNotificationPack(
        personalization: Personalization,
        userId: UserId,
    ) {
        val owner = PersonalizationOwner.User(userId)
        val activeOwnership =
            ownershipRepository.findActive(
                owner = owner,
                type = PersonalizationType.NOTIFICATION_PACK,
            ) ?: return

        if (activeOwnership.personalizationId != personalization.id) {
            return
        }

        notificationPackSelectionEventPublisher.publishSelected(
            userId = userId,
            personalization = personalization,
        )
    }

    private fun requireOwnedForUpdate(
        personalizationId: PersonalizationId,
        userId: UserId,
    ): Personalization {
        val personalization =
            repository.findByIdForUpdate(personalizationId)
                ?: throw PersonalizationNotFoundException()

        if (personalization.creatorUserId != userId) {
            throw PersonalizationAccessDeniedException()
        }

        return personalization
    }
}
