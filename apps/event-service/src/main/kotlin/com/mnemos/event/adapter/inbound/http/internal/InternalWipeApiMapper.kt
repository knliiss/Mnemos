package com.mnemos.event.adapter.inbound.http.internal

import com.mnemos.event.domain.wipe.model.Wipe
import java.time.Instant
import com.mnemos.event.contracts.internal.v1.InternalWipeResponse as InternalWipeContractResponse

object InternalWipeApiMapper {
    fun Wipe.toResponse(): InternalWipeResponse =
        InternalWipeResponse(
            id = id.id,
            startedAt = startedAt,
            endedAt = endedAt,
        )

    fun Wipe.toContractResponse(now: Instant): InternalWipeContractResponse =
        InternalWipeContractResponse(
            id = id.id,
            number = number,
            name = name,
            startedAt = startedAt,
            endedAt = endedAt,
            active = contains(now),
        )
}
