package com.mnemos.social.domain.spotlight.model

import com.github.f4b6a3.uuid.UuidCreator
import java.util.UUID

@JvmInline
value class DropSpotlightId(
    val id: UUID,
) {
    companion object {
        fun generate(): DropSpotlightId = DropSpotlightId(UuidCreator.getTimeOrderedEpoch())
    }
}
