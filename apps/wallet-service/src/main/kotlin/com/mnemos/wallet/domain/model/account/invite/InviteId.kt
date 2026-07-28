package com.mnemos.accountservice.domain.model.account.invite

import java.util.UUID

@JvmInline
value class InviteId(
    val value: UUID,
) {
    companion object {
        fun generate() = InviteId(UUID.randomUUID())
    }
}
