package com.mnemos.accountservice.domain.model.account.role

import java.util.UUID

@JvmInline
value class RoleId(
    val value: UUID,
) {
    companion object {
        fun generate() = RoleId(UUID.randomUUID())
    }
}
