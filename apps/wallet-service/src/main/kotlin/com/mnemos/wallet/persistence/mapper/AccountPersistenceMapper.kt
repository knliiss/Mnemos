package com.mnemos.accountservice.persistence.mapper

import com.mnemos.accountservice.domain.model.account.Account
import com.mnemos.accountservice.domain.model.account.AccountMemberId
import com.mnemos.accountservice.domain.model.account.CristalixNickname
import com.mnemos.accountservice.domain.model.account.CristalixUuid
import com.mnemos.accountservice.domain.model.account.role.RoleId
import com.mnemos.accountservice.persistence.entity.account.AccountEntity
import com.mnemos.common.core.model.id.AccountId

object AccountPersistenceMapper {
    fun Account.toEntity() =
        AccountEntity(
            id = id.value,
            ownerMemberId = ownerMemberId.value,
            cristalixUuid = cristalixUuid.value,
            cristalixNickname = cristalixNickname.value,
            createdAt = createdAt,
            updatedAt = updatedAt,
            deletedAt = deletedAt,
            deletionRequestedAt = deletionRequestedAt,
            status = status,
            maxMembers = maxMembers,
            baseRoleId = baseRoleId?.value,
        )

    fun AccountEntity.toDomain() =
        Account.restore(
            id = AccountId(id),
            ownerMemberId = AccountMemberId(ownerMemberId),
            cristalixUuid = CristalixUuid(cristalixUuid),
            cristalixNickname = CristalixNickname(cristalixNickname),
            createdAt = createdAt,
            updatedAt = updatedAt,
            deletedAt = deletedAt,
            deletionRequestedAt = deletionRequestedAt,
            status = status,
            maxMembers = maxMembers,
            baseRoleId = baseRoleId?.let { RoleId(it) },
        )

    fun AccountEntity.updateFrom(account: Account) {
        cristalixNickname = account.cristalixNickname.value
        updatedAt = account.updatedAt

        deletionRequestedAt = account.deletionRequestedAt
        deletedAt = account.deletedAt
        status = account.status

        maxMembers = account.maxMembers
        baseRoleId = account.baseRoleId?.value
    }
}
