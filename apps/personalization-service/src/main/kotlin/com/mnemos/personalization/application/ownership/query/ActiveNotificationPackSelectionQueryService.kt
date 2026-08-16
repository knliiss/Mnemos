package com.mnemos.personalization.application.ownership.query

import com.mnemos.common.core.model.id.UserId
import com.mnemos.personalization.application.ownership.event.port.out.NotificationPackSelectionRevisionStore
import com.mnemos.personalization.domain.ownership.model.PersonalizationOwner
import com.mnemos.personalization.domain.ownership.repository.OwnedPersonalizationRepository
import com.mnemos.personalization.domain.personalization.model.PersonalizationContent
import com.mnemos.personalization.domain.personalization.model.PersonalizationType
import com.mnemos.personalization.domain.personalization.repository.PersonalizationRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
class ActiveNotificationPackSelectionQueryService(
    private val ownershipRepository: OwnedPersonalizationRepository,
    private val personalizationRepository: PersonalizationRepository,
    private val revisionStore: NotificationPackSelectionRevisionStore,
) {
    @Transactional(readOnly = true)
    fun find(userId: UUID): ActiveNotificationPackSelection? {
        val typedUserId = UserId(userId)
        val owner = PersonalizationOwner.User(typedUserId)
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
            userId = typedUserId,
            personalizationId = personalization.id,
            selectionRevision = revisionStore.current(typedUserId),
            content = content,
        )
    }
}
