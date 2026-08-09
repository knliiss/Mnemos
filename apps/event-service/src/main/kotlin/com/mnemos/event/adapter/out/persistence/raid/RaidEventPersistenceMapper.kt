package com.mnemos.event.adapter.out.persistence.raid

import com.mnemos.event.domain.EventId
import com.mnemos.event.domain.raid.model.RaidEvent

object RaidEventPersistenceMapper {
    fun RaidEvent.toEntity(): RaidEventEntity =
        RaidEventEntity(
            id = id.id,
            locations = locations,
            startedAt = startedAt,
        )

    fun RaidEventEntity.toDomain(): RaidEvent =
        RaidEvent(
            id = EventId(id),
            locations = locations,
            startedAt = startedAt,
        )
}
