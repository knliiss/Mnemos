package com.mnemos.personalization.application.ownership.usecase.notificationpack

import com.mnemos.common.core.model.id.TelegramId
import com.mnemos.personalization.application.account.port.out.PersonalizationTelegramUserResolver
import com.mnemos.personalization.application.ownership.event.NotificationPackSelectionEventPublisher
import com.mnemos.personalization.application.ownership.model.TelegramNotificationPackOption
import com.mnemos.personalization.domain.ownership.model.PersonalizationOwner
import com.mnemos.personalization.domain.ownership.repository.OwnedPersonalizationRepository
import com.mnemos.personalization.domain.personalization.model.PersonalizationId
import com.mnemos.personalization.domain.personalization.model.PersonalizationType
import com.mnemos.personalization.domain.personalization.repository.PersonalizationRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class TelegramNotificationPackManagementService(
    private val telegramUserResolver: PersonalizationTelegramUserResolver,
    private val ownershipRepository: OwnedPersonalizationRepository,
    private val personalizationRepository: PersonalizationRepository,
    private val selectionEventPublisher: NotificationPackSelectionEventPublisher,
) {
    @Transactional(readOnly = true)
    fun listOwned(telegramId: TelegramId): List<TelegramNotificationPackOption>? {
        val userId = telegramUserResolver.findUserId(telegramId) ?: return null
        val owner = PersonalizationOwner.User(userId)

        return ownershipRepository
            .findAll(owner)
            .asSequence()
            .filter { ownership -> ownership.type == PersonalizationType.NOTIFICATION_PACK }
            .map { ownership ->
                val personalization =
                    personalizationRepository.findById(ownership.personalizationId)
                        ?: error("Owned notification pack personalization was not found")

                TelegramNotificationPackOption(
                    personalizationId = personalization.id,
                    name = personalization.name,
                    active = ownership.active,
                )
            }.sortedWith(
                compareByDescending<TelegramNotificationPackOption> { option -> option.active }
                    .thenBy { option -> option.name.lowercase() },
            ).toList()
    }

    @Transactional
    fun activate(
        telegramId: TelegramId,
        personalizationId: PersonalizationId,
    ): Boolean {
        val userId = telegramUserResolver.findUserId(telegramId) ?: return false
        val owner = PersonalizationOwner.User(userId)
        val personalization = personalizationRepository.findByIdForUpdate(personalizationId) ?: return false

        if (personalization.type != PersonalizationType.NOTIFICATION_PACK) {
            return false
        }

        val ownership = ownershipRepository.findForUpdate(owner, personalizationId) ?: return false

        if (!ownership.active) {
            ownershipRepository.deactivateActive(
                owner = owner,
                type = PersonalizationType.NOTIFICATION_PACK,
            )
            ownership.activate()
            ownershipRepository.save(ownership)
        }

        selectionEventPublisher.publishSelected(
            userId = userId,
            personalization = personalization,
        )

        return true
    }
}
