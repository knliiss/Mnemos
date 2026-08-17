package com.mnemos.personalization.application.ownership.usecase.getactivepack

import com.mnemos.common.core.model.id.UserId
import com.mnemos.personalization.application.ownership.event.port.out.NotificationPackSelectionRevisionStore
import com.mnemos.personalization.application.ownership.model.ActiveNotificationPackSelection
import com.mnemos.personalization.domain.ownership.model.PersonalizationOwner
import com.mnemos.personalization.domain.ownership.repository.OwnedPersonalizationRepository
import com.mnemos.personalization.domain.personalization.model.PersonalizationContent
import com.mnemos.personalization.domain.personalization.model.PersonalizationType
import com.mnemos.personalization.domain.personalization.repository.PersonalizationRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class GetActiveNotificationPackService(
    private val ownershipRepository: OwnedPersonalizationRepository,
    private val personalizationRepository: PersonalizationRepository,
    private val revisionStore: NotificationPackSelectionRevisionStore,
) : GetActiveNotificationPackUseCase {
    @Transactional(readOnly = true)
    override fun execute(userId: UserId): ActiveNotificationPackSelection? {
        val owner = PersonalizationOwner.User(userId)
        val ownership =
            ownershipRepository.findActive(
                owner = owner,
                type = PersonalizationType.NOTIFICATION_PACK,
            ) ?: return null
        val personalization =
            personalizationRepository.findById(ownership.personalizationId)
                ?: error("Active notification pack personalization was not found")
        val content =
            personalization.content as? PersonalizationContent.NotificationPack
                ?: error("Active notification pack has invalid content type")

        return ActiveNotificationPackSelection(
            userId = userId,
            personalizationId = personalization.id,
            selectionRevision = revisionStore.current(userId),
            content = content,
        )
    }
}
