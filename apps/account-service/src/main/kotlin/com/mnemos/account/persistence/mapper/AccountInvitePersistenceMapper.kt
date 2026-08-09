package com.mnemos.account.persistence.mapper
import com.mnemos.account.domain.model.account.invite.AccountInvite
import com.mnemos.account.domain.model.account.invite.InviteCode
import com.mnemos.account.domain.model.account.invite.InviteId
import com.mnemos.account.persistence.entity.invite.AccountInviteEntity
import com.mnemos.common.core.model.id.AccountId
import com.mnemos.common.core.model.id.UserId

object AccountInvitePersistenceMapper {
    fun AccountInvite.toEntity(): AccountInviteEntity =
        AccountInviteEntity(
            id = id.id,
            accountId = accountId.id,
            code = code.value,
            createdByUserId = createdByUserId.id,
            createdAt = createdAt,
            expiresAt = expiresAt,
            consumedAt = consumedAt,
            revokedAt = revokedAt,
        )

    fun AccountInviteEntity.toDomain(): AccountInvite =
        AccountInvite.restore(
            id = InviteId(id),
            accountId = AccountId(accountId),
            code = InviteCode.of(code),
            createdByUserId = UserId(createdByUserId),
            createdAt = createdAt,
            expiresAt = expiresAt,
            consumedAt = consumedAt,
            revokedAt = revokedAt,
        )
}
