package com.mnemos.account.domain.model.account

import com.github.f4b6a3.uuid.UuidCreator
import java.util.UUID

@JvmInline
value class AccountMemberId(
    val id: UUID,
) {
    companion object {
        fun generate(): AccountMemberId = AccountMemberId(UuidCreator.getTimeOrderedEpoch())
    }
}
