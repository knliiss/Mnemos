package com.mnemos.account.domain.model.account.role

import java.util.UUID

@JvmInline
value class RoleId(
    val id: UUID,
) {
    companion object {
        fun generate() = RoleId(UUID.randomUUID())
    }
}
