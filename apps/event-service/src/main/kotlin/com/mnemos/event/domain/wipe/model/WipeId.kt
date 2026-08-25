package com.mnemos.event.domain.wipe.model

import com.github.f4b6a3.uuid.UuidCreator
import java.util.UUID

@JvmInline
value class WipeId(
    val id: UUID,
) {
    companion object {
        fun generate() = WipeId(UuidCreator.getTimeOrderedEpoch())
    }
}
