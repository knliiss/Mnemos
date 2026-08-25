package com.mnemos.event.adapter.inbound.http.wipe

import com.mnemos.event.domain.wipe.model.Wipe
import java.time.Instant

object WipeApiMapper {
    fun Wipe.toResponse(activeAt: Instant): WipeResponse =
        WipeResponse(
            id = id.id,
            number = number,
            name = name,
            startedAt = startedAt,
            endedAt = endedAt,
            active = contains(activeAt),
            createdBy = createdBy,
            createdAt = createdAt,
        )
}
