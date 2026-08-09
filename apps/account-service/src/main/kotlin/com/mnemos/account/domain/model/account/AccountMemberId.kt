package com.mnemos.account.domain.model.account

import java.util.UUID

@JvmInline
value class AccountMemberId(
    val id: UUID,
) {
    companion object {
        fun generate(): AccountMemberId = AccountMemberId(UUID.randomUUID())
    }
}
