package com.mnemos.accountservice.persistence.mapper
import com.mnemos.accountservice.domain.model.account.invite.AccountInvite
import com.mnemos.accountservice.domain.model.account.invite.InviteCode
import com.mnemos.accountservice.domain.model.account.invite.InviteId
import com.mnemos.accountservice.persistence.entity.invite.AccountInviteEntity
import com.mnemos.common.core.model.id.AccountId
import com.mnemos.common.core.model.user.UserId

object AccountInvitePersistenceMapper {
    fun AccountInvite.toEntity(): AccountInviteEntity =
        AccountInviteEntity(
            id = id.value,
            accountId = accountId.value,
            code = code.value,
            createdByUserId = createdByUserId.value,
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
