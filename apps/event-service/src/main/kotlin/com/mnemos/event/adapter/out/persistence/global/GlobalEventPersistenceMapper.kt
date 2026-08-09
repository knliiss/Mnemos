package com.mnemos.event.adapter.out.persistence.global

import com.mnemos.event.domain.EventId
import com.mnemos.event.domain.global.model.GlobalEvent

object GlobalEventPersistenceMapper {
    fun GlobalEvent.toEntity(): GlobalEventEntity =
        GlobalEventEntity(
            id = id.id,
            type = type,
            startedAt = startedAt,
        )

    fun GlobalEventEntity.toDomain(): GlobalEvent =
        GlobalEvent(
            id = EventId(id),
            type = type,
            startedAt = startedAt,
        )
}
