package com.mnemos.event.adapter.out.persistence.booster

import com.mnemos.event.domain.EventId
import com.mnemos.event.domain.booster.model.BoosterEvent
import com.mnemos.event.domain.player.model.CristalixPlayerId

object BoosterEventPersistenceMapper {
    fun BoosterEvent.toEntity(): BoosterEventEntity =
        BoosterEventEntity(
            id = id.id,
            type = type,
            playerId = playerId?.id,
            activatedBy = activatedBy,
            activatedAt = activatedAt,
            identityResolutionPending = identityResolutionPending,
            identityResolutionRequestedAt = identityResolutionRequestedAt,
        )

    fun BoosterEventEntity.toDomain(): BoosterEvent =
        BoosterEvent(
            id = EventId(id),
            type = type,
            playerId = playerId?.let(::CristalixPlayerId),
            activatedBy = activatedBy,
            activatedAt = activatedAt,
            identityResolutionPending = identityResolutionPending,
            identityResolutionRequestedAt = identityResolutionRequestedAt,
        )
}
