package com.mnemos.account.domain.model.account.invite

import java.util.UUID

@JvmInline
value class InviteId(
    val id: UUID,
) {
    companion object {
        fun generate() = InviteId(UUID.randomUUID())
    }
}
