package com.mnemos.account.domain.model.account.invite

import com.github.f4b6a3.uuid.UuidCreator
import java.util.UUID

@JvmInline
value class InviteId(
    val id: UUID,
) {
    companion object {
        fun generate() = InviteId(UuidCreator.getTimeOrderedEpoch())
    }
}
