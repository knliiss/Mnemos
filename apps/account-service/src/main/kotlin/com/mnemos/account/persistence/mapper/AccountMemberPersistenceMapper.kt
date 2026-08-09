package com.mnemos.account.persistence.mapper

import com.mnemos.account.domain.model.account.AccountMember
import com.mnemos.account.domain.model.account.AccountMemberId
import com.mnemos.account.persistence.entity.account.AccountMemberEntity
import com.mnemos.common.core.model.id.AccountId
import com.mnemos.common.core.model.id.UserId

object AccountMemberPersistenceMapper {
    fun AccountMember.toEntity() =
        AccountMemberEntity(
            id = id.id,
            accountId = accountId.id,
            userId = userId.id,
            joinedAt = joinedAt,
        )

    fun AccountMemberEntity.toDomain() =
        AccountMember.restore(
            id = AccountMemberId(id),
            accountId = AccountId(accountId),
            userId = UserId(userId),
            joinedAt = joinedAt,
        )
}
