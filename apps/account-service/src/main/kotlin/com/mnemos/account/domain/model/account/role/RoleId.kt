package com.mnemos.account.domain.model.account.role

import com.github.f4b6a3.uuid.UuidCreator
import java.util.UUID

@JvmInline
value class RoleId(
    val id: UUID,
) {
    companion object {
        fun generate() = RoleId(UuidCreator.getTimeOrderedEpoch())
    }
}
