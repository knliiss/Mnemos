package com.mnemos.accountservice.persistence.mapper

import com.mnemos.accountservice.domain.model.account.Account
import com.mnemos.accountservice.domain.model.account.CristalixNickname
import com.mnemos.accountservice.domain.model.account.CristalixUuid
import com.mnemos.accountservice.persistence.entity.AccountEntity
import com.mnemos.common.core.model.id.AccountId
import com.mnemos.common.core.model.user.UserId

object AccountPersistenceMapper {
    fun Account.toEntity() = AccountEntity(
        id = id.value,
        ownerUserId = ownerUserId.value,
        cristalixUuid = cristalixUuid.value,
        cristalixNickname = cristalixNickname.value,
        createdAt = createdAt,
        updatedAt = updatedAt
    )

    fun AccountEntity.toDomain() = Account.restore(
        id = AccountId(id),
        ownerUserId = UserId(ownerUserId),
        cristalixUuid = CristalixUuid(cristalixUuid),
        cristalixNickname = CristalixNickname(cristalixNickname),
        createdAt = createdAt,
        updatedAt = updatedAt
    )

    fun AccountEntity.updateFrom(account: Account) {
        cristalixNickname = account.cristalixNickname.value
        updatedAt = account.updatedAt
    }
}