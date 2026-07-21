package com.mnemos.accountservice.domain.model.account

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

        fun create(accountId: AccountId, userId: UserId): AccountMember {
            val now = Instant.now()
            return AccountMember(
                id = AccountMemberId.generate(),
                accountId = accountId,
                userId = userId,
                joinedAt = now
            )
        }

        fun restore(
            id: AccountMemberId,
            accountId: AccountId,
            userId: UserId,
            joinedAt: Instant,
        ): AccountMember {
            return AccountMember(id, accountId, userId, joinedAt)
        }
    }

}