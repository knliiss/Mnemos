package com.mnemos.personalization.application.personalization.moderation

import com.mnemos.personalization.application.personalization.exception.PersonalizationAccessDeniedException
import com.mnemos.personalization.application.personalization.exception.PersonalizationNotFoundException
import com.mnemos.personalization.application.pricing.PersonalizationPricePolicy
import com.mnemos.personalization.application.security.model.PersonalizationActor
import com.mnemos.personalization.application.security.port.out.CurrentPersonalizationActorProvider
import com.mnemos.personalization.domain.personalization.model.Personalization
import com.mnemos.personalization.domain.personalization.model.PersonalizationId
import com.mnemos.personalization.domain.personalization.model.PersonalizationPrice
import com.mnemos.personalization.domain.personalization.model.PersonalizationStatus
import com.mnemos.personalization.domain.personalization.repository.PersonalizationRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Clock

@Service
class ModeratePersonalizationService(
    private val actorProvider: CurrentPersonalizationActorProvider,
    private val pricePolicy: PersonalizationPricePolicy,
    private val repository: PersonalizationRepository,
    private val clock: Clock,
) {
    @Transactional
    fun approve(
        personalizationId: PersonalizationId,
        price: PersonalizationPrice,
    ): Personalization {
        val actor = requireModerator()
        val personalization = requirePersonalization(personalizationId)

        requireCanModerate(
            actor = actor,
            personalization = personalization,
        )
        pricePolicy.requireAllowed(
            type = personalization.type,
            origin = personalization.origin,
            price = price,
        )
        personalization.approve(
            moderatedPrice = price,
            now = clock.instant(),
        )

        return repository.save(personalization)
    }

    @Transactional
    fun reject(
        personalizationId: PersonalizationId,
        reason: String,
    ): Personalization {
        val actor = requireModerator()
        val personalization = requirePersonalization(personalizationId)

        requireCanModerate(
            actor = actor,
            personalization = personalization,
        )
        personalization.reject(
            reason = reason,
            now = clock.instant(),
        )

        return repository.save(personalization)
    }

    @Transactional
    fun unlist(personalizationId: PersonalizationId): Personalization {
        val actor = requireModerator()
        val personalization = requirePersonalization(personalizationId)

        requireCanModerate(
            actor = actor,
            personalization = personalization,
        )
        personalization.unlist(clock.instant())

        return repository.save(personalization)
    }

    @Transactional
    fun relist(personalizationId: PersonalizationId): Personalization {
        val actor = requireModerator()
        val personalization = requirePersonalization(personalizationId)

        requireCanModerate(
            actor = actor,
            personalization = personalization,
        )
        personalization.relist(clock.instant())

        return repository.save(personalization)
    }

    fun listReviewQueue(): List<Personalization> {
        requireModerator()

        return repository.findByStatus(PersonalizationStatus.IN_REVIEW)
    }

    private fun requireModerator(): PersonalizationActor {
        val actor = actorProvider.requireActor()

        if (!actor.canModeratePersonalization()) {
            throw PersonalizationAccessDeniedException()
        }

        return actor
    }

    private fun requireCanModerate(
        actor: PersonalizationActor,
        personalization: Personalization,
    ) {
        if (
            personalization.creatorUserId == actor.userId &&
            !actor.canModerateOwnPersonalization()
        ) {
            throw PersonalizationAccessDeniedException()
        }
    }

    private fun requirePersonalization(personalizationId: PersonalizationId): Personalization =
        repository.findById(personalizationId)
            ?: throw PersonalizationNotFoundException()
}
