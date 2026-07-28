package com.mnemos.account.domain.model.account.invite

import com.mnemos.account.domain.exception.invite.AccountInviteUnavailableException
import com.mnemos.common.core.model.id.AccountId
import com.mnemos.common.core.model.user.UserId
import java.time.Instant

class AccountInvite private constructor(
    val id: InviteId,
    val accountId: AccountId,
    val code: InviteCode,
    val createdByUserId: UserId,
    val createdAt: Instant,
    val expiresAt: Instant?,
    consumedAt: Instant?,
    revokedAt: Instant?,
) {
    var consumedAt = consumedAt
        private set

    var revokedAt = revokedAt
        private set

    fun consume(now: Instant = Instant.now()) {
        if (!isAvailableAt(now)) {
            throw AccountInviteUnavailableException()
        }

        consumedAt = now
    }

    fun revoke(now: Instant = Instant.now()): Boolean {
        if (revokedAt != null || consumedAt != null) return false

        revokedAt = now
        return true
    }

    fun isAvailableAt(now: Instant = Instant.now()): Boolean =
        consumedAt == null &&
            revokedAt == null &&
            (expiresAt == null || expiresAt.isAfter(now))

    companion object {
        fun create(
            accountId: AccountId,
            code: InviteCode,
            createdByUserId: UserId,
            expiresAt: Instant?,
            now: Instant = Instant.now(),
        ): AccountInvite {
            require(expiresAt == null || expiresAt.isAfter(now)) {
                "Invite expiration time must be in the future"
            }

            return AccountInvite(
                id = InviteId.generate(),
                accountId = accountId,
                code = code,
                createdByUserId = createdByUserId,
                createdAt = now,
                expiresAt = expiresAt,
                consumedAt = null,
                revokedAt = null,
            )
        }

        fun restore(
            id: InviteId,
            accountId: AccountId,
            code: InviteCode,
            createdByUserId: UserId,
            createdAt: Instant,
            expiresAt: Instant?,
            consumedAt: Instant?,
            revokedAt: Instant?,
        ): AccountInvite =
            AccountInvite(
                id = id,
                accountId = accountId,
                code = code,
                createdByUserId = createdByUserId,
                createdAt = createdAt,
                expiresAt = expiresAt,
                consumedAt = consumedAt,
                revokedAt = revokedAt,
            )
    }
}
