package com.mnemos.personalization.application.personalization.support

import com.mnemos.personalization.application.personalization.exception.PersonalizationAccessDeniedException
import com.mnemos.personalization.application.personalization.exception.PersonalizationNotFoundException
import com.mnemos.personalization.application.security.model.PersonalizationActor
import com.mnemos.personalization.application.security.port.out.CurrentPersonalizationActorProvider
import com.mnemos.personalization.domain.personalization.model.Personalization
import com.mnemos.personalization.domain.personalization.model.PersonalizationId
import com.mnemos.personalization.domain.personalization.repository.PersonalizationRepository
import org.springframework.stereotype.Component

@Component
class PersonalizationModerationSupport(
    private val actorProvider: CurrentPersonalizationActorProvider,
    private val repository: PersonalizationRepository,
) {
    fun requireModerator(): PersonalizationActor {
        val actor = actorProvider.requireActor()

        if (!actor.canModeratePersonalization()) {
            throw PersonalizationAccessDeniedException()
        }

        return actor
    }

    fun requireCanModerate(
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

    fun requirePersonalizationForUpdate(personalizationId: PersonalizationId): Personalization =
        repository.findByIdForUpdate(personalizationId)
            ?: throw PersonalizationNotFoundException()
}
