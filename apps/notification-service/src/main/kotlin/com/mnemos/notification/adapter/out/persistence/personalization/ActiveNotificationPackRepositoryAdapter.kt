package com.mnemos.notification.adapter.out.persistence.personalization

import com.mnemos.common.core.model.id.UserId
import com.mnemos.notification.adapter.out.persistence.personalization.ActiveNotificationPackPersistenceMapper.toDomainOrNull
import com.mnemos.notification.adapter.out.persistence.personalization.ActiveNotificationPackPersistenceMapper.toTemplateDocuments
import com.mnemos.notification.application.notification.personalization.model.ActiveNotificationPack
import com.mnemos.notification.application.notification.personalization.port.out.ActiveNotificationPackRepository
import org.springframework.stereotype.Repository
import java.time.Instant
import java.util.UUID

@Repository
class ActiveNotificationPackRepositoryAdapter(
    private val repository: ActiveNotificationPackJpaRepository,
) : ActiveNotificationPackRepository {
    override fun find(userId: UserId): ActiveNotificationPack? =
        repository.findById(userId.id).orElse(null)?.toDomainOrNull()

    override fun apply(
        userId: UserId,
        pack: ActiveNotificationPack?,
        eventId: UUID,
        occurredAt: Instant,
    ) {
        repository.ensureState(userId.id)

        val state =
            requireNotNull(repository.findByUserIdForUpdate(userId.id)) {
                "Notification pack projection state was not created"
            }

        if (!state.shouldApply(eventId, occurredAt)) {
            return
        }

        if (pack == null) {
            state.clear(
                eventId = eventId,
                occurredAt = occurredAt,
            )
        } else {
            state.select(
                personalizationId = pack.personalizationId,
                mixedAggregateTitle = pack.mixedAggregateTitle,
                templates = pack.toTemplateDocuments(),
                eventId = eventId,
                occurredAt = occurredAt,
            )
        }

        repository.save(state)
    }
}
