package com.mnemos.accountservice.persistence.mapper

import com.mnemos.accountservice.domain.model.account.AccountMember
import com.mnemos.accountservice.domain.model.account.AccountMemberId
import com.mnemos.accountservice.persistence.entity.account.AccountMemberEntity
import com.mnemos.common.core.model.id.AccountId
import com.mnemos.common.core.model.user.UserId

object AccountMemberPersistenceMapper {
    fun AccountMember.toEntity() =
        AccountMemberEntity(
            id = id.value,
            accountId = accountId.value,
            userId = userId.value,
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
