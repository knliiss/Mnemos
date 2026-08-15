package com.mnemos.personalization.application.personalization.create

import com.mnemos.personalization.application.pricing.PersonalizationPricePolicy
import com.mnemos.personalization.application.security.port.out.CurrentPersonalizationActorProvider
import com.mnemos.personalization.domain.ownership.model.OwnedPersonalization
import com.mnemos.personalization.domain.ownership.model.PersonalizationOwner
import com.mnemos.personalization.domain.ownership.repository.OwnedPersonalizationRepository
import com.mnemos.personalization.domain.personalization.model.Personalization
import com.mnemos.personalization.domain.personalization.model.PersonalizationContent
import com.mnemos.personalization.domain.personalization.model.PersonalizationOrigin
import com.mnemos.personalization.domain.personalization.model.PersonalizationType
import com.mnemos.personalization.domain.personalization.repository.PersonalizationRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Clock

data class CreateCustomNotificationPackCommand(
    val name: String,
    val content: PersonalizationContent.NotificationPack,
)

data class CreateCustomNotificationPackResult(
    val personalization: Personalization,
    val grantedForFree: Boolean,
)

@Service
class CreateCustomNotificationPackService(
    private val actorProvider: CurrentPersonalizationActorProvider,
    private val personalizationRepository: PersonalizationRepository,
    private val ownershipRepository: OwnedPersonalizationRepository,
    private val pricePolicy: PersonalizationPricePolicy,
    private val clock: Clock,
) {
    @Transactional
    fun execute(command: CreateCustomNotificationPackCommand): CreateCustomNotificationPackResult {
        val actor = actorProvider.requireActor()
        val firstCreatorCustomPack =
            actor.canCreateMarketplacePersonalization() &&
                personalizationRepository
                    .findByCreatorUserId(actor.userId)
                    .none { personalization ->
                        personalization.origin == PersonalizationOrigin.CUSTOM &&
                            personalization.type == PersonalizationType.NOTIFICATION_PACK
                    }

        val personalization =
            Personalization.createCustomNotificationPack(
                creatorUserId = actor.userId,
                name = command.name,
                content = command.content,
                price =
                    if (firstCreatorCustomPack) {
                        null
                    } else {
                        pricePolicy.customNotificationPackPurchasePrice()
                    },
                now = clock.instant(),
            )

        val saved = personalizationRepository.save(personalization)

        if (firstCreatorCustomPack) {
            ownershipRepository.save(
                OwnedPersonalization(
                    owner = PersonalizationOwner.User(actor.userId),
                    personalizationId = saved.id,
                    type = saved.type,
                    acquiredAt = clock.instant(),
                    active = false,
                ),
            )
        }

        return CreateCustomNotificationPackResult(
            personalization = saved,
            grantedForFree = firstCreatorCustomPack,
        )
    }
}
