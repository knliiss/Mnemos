package com.mnemos.event.adapter.out.persistence.booster

import com.mnemos.event.domain.EventId
import com.mnemos.event.domain.booster.model.BoosterEvent

object BoosterEventPersistenceMapper {
    fun BoosterEvent.toEntity(): BoosterEventEntity =
        BoosterEventEntity(
            id = id.id,
            type = type,
            activatedBy = activatedBy,
            activatedAt = activatedAt,
        )

    fun BoosterEventEntity.toDomain(): BoosterEvent =
        BoosterEvent(
            id = EventId(id),
            type = type,
            activatedBy = activatedBy,
            activatedAt = activatedAt,
        )
}
