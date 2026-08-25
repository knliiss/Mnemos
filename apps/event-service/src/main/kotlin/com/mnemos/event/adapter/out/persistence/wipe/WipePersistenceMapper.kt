package com.mnemos.event.adapter.out.persistence.wipe

import com.mnemos.event.domain.wipe.model.Wipe
import com.mnemos.event.domain.wipe.model.WipeId

object WipePersistenceMapper {
    fun Wipe.toEntity(): WipeEntity =
        WipeEntity(
            id = id.id,
            number = number,
            name = name,
            startedAt = startedAt,
            endedAt = endedAt,
            createdBy = createdBy,
            createdAt = createdAt,
        )

    fun WipeEntity.toDomain(): Wipe =
        Wipe(
            id = WipeId(id),
            number = number,
            name = name,
            startedAt = startedAt,
            endedAt = endedAt,
            createdBy = createdBy,
            createdAt = createdAt,
        )
}
