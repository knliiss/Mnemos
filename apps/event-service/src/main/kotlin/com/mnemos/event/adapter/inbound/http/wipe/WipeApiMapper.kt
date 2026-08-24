package com.mnemos.event.adapter.inbound.http.wipe

import com.mnemos.event.domain.wipe.model.Wipe

object WipeApiMapper {
    fun Wipe.toResponse(): WipeResponse =
        WipeResponse(
            id = id.id,
            number = number,
            name = name,
            startedAt = startedAt,
            endedAt = endedAt,
            active = active,
            createdBy = createdBy,
            createdAt = createdAt,
        )
}
