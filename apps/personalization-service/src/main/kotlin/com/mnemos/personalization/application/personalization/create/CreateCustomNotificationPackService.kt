package com.mnemos.personalization.application.personalization.create

import com.mnemos.personalization.application.pricing.PersonalizationPricePolicy
import com.mnemos.personalization.application.security.port.out.CurrentPersonalizationActorProvider
import com.mnemos.personalization.domain.ownership.model.OwnedPersonalization
import com.mnemos.personalization.domain.ownership.model.PersonalizationOwner
import com.mnemos.personalization.domain.ownership.repository.OwnedPersonalizationRepository
import com.mnemos.personalization.domain.personalization.model.Personalization
import com.mnemos.personalization.domain.personalization.model.PersonalizationContent
import com.mnemos.personalization.domain.personalization.repository.CustomNotificationPackFreeGrantRepository
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
    private val freeGrantRepository: CustomNotificationPackFreeGrantRepository,
    private val pricePolicy: PersonalizationPricePolicy,
    private val clock: Clock,
) {
    @Transactional
    fun execute(command: CreateCustomNotificationPackCommand): CreateCustomNotificationPackResult {
        val actor = actorProvider.requireActor()
        val now = clock.instant()
        val grantedForFree =
            actor.isPersonalizationCreator() &&
                freeGrantRepository.claim(
                    userId = actor.userId,
                    claimedAt = now,
                )

        val personalization =
            Personalization.createCustomNotificationPack(
                creatorUserId = actor.userId,
                name = command.name,
                content = command.content,
                price =
                    if (grantedForFree) {
                        null
                    } else {
                        pricePolicy.customNotificationPackPurchasePrice()
                    },
                now = now,
            )

        val saved = personalizationRepository.save(personalization)

        if (grantedForFree) {
            ownershipRepository.save(
                OwnedPersonalization(
                    owner = PersonalizationOwner.User(actor.userId),
                    personalizationId = saved.id,
                    type = saved.type,
                    acquiredAt = now,
                    active = false,
                ),
            )
        }

        return CreateCustomNotificationPackResult(
            personalization = saved,
            grantedForFree = grantedForFree,
        )
    }
}
