package com.mnemos.personalization.application.personalization.usecase.updatecustompack

import com.mnemos.personalization.application.personalization.exception.PersonalizationAccessDeniedException
import com.mnemos.personalization.application.personalization.support.PersonalizationCreatorSupport
import com.mnemos.personalization.domain.personalization.model.Personalization
import com.mnemos.personalization.domain.personalization.model.PersonalizationOrigin
import com.mnemos.personalization.domain.personalization.model.PersonalizationType
import com.mnemos.personalization.domain.personalization.repository.PersonalizationRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Clock

@Service
class UpdateCustomNotificationPackService(
    private val support: PersonalizationCreatorSupport,
    private val repository: PersonalizationRepository,
    private val clock: Clock,
) : UpdateCustomNotificationPackUseCase {
    @Transactional
    override fun execute(command: UpdateCustomNotificationPackCommand): Personalization {
        val actor = support.requireActor()
        val personalization = support.requireOwnedForUpdate(command.personalizationId, actor.userId)

        if (
            personalization.origin != PersonalizationOrigin.CUSTOM ||
            personalization.type != PersonalizationType.NOTIFICATION_PACK
        ) {
            throw PersonalizationAccessDeniedException()
        }

        personalization.updateDraft(
            name = command.name,
            content = command.content,
            price = personalization.price,
            now = clock.instant(),
        )

        val saved = repository.save(personalization)

        support.republishActiveNotificationPack(
            personalization = saved,
            userId = actor.userId,
        )

        return saved
    }
}
