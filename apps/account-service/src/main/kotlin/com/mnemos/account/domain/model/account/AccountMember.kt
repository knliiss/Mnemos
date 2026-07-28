package com.mnemos.account.domain.model.account

import com.mnemos.common.core.model.id.AccountId
import com.mnemos.common.core.model.user.UserId
import java.time.Instant

class AccountMember private constructor(
    val id: AccountMemberId,
    val accountId: AccountId,
    val userId: UserId,
    val joinedAt: Instant,
) {
    companion object {
        fun create(
            id: AccountMemberId,
            accountId: AccountId,
            userId: UserId,
        ): AccountMember =
            AccountMember(
                id = id,
                accountId = accountId,
                userId = userId,
                joinedAt = Instant.now(),
            )

        fun restore(
            id: AccountMemberId,
            accountId: AccountId,
            userId: UserId,
            joinedAt: Instant,
        ): AccountMember =
            AccountMember(
                id = id,
                accountId = accountId,
                userId = userId,
                joinedAt = joinedAt,
            )
    }
}
