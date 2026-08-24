package com.mnemos.event.adapter.inbound.http.internal

import com.mnemos.event.domain.wipe.model.Wipe

object InternalWipeApiMapper {
    fun Wipe.toResponse(): InternalWipeResponse =
        InternalWipeResponse(
            id = id.id,
            startedAt = startedAt,
            endedAt = endedAt,
        )
}
