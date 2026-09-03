package com.mnemos.account.persistence.mapper

import com.mnemos.account.domain.model.account.Account
import com.mnemos.account.domain.model.account.AccountMemberId
import com.mnemos.account.domain.model.account.CristalixNickname
import com.mnemos.account.domain.model.account.CristalixUuid
import com.mnemos.account.domain.model.account.role.RoleId
import com.mnemos.account.persistence.entity.account.AccountEntity
import com.mnemos.common.core.model.id.AccountId

object AccountPersistenceMapper {
    fun Account.toEntity() =
        AccountEntity(
            id = id.id,
            ownerMemberId = ownerMemberId.id,
            cristalixUuid = cristalixUuid.id,
            cristalixNickname = cristalixNickname.value,
            createdAt = createdAt,
            updatedAt = updatedAt,
            status = status,
            deactivatedAt = deactivatedAt,
            deletedAt = deletedAt,
            maxMembers = maxMembers,
            baseRoleId = baseRoleId?.id,
        )

    fun AccountEntity.toDomain() =
        Account.restore(
            id = AccountId(id),
            ownerMemberId = AccountMemberId(ownerMemberId),
            cristalixUuid = CristalixUuid(cristalixUuid),
            cristalixNickname = CristalixNickname(cristalixNickname),
            createdAt = createdAt,
            updatedAt = updatedAt,
            status = status,
            deactivatedAt = deactivatedAt,
            deletedAt = deletedAt,
            maxMembers = maxMembers,
            baseRoleId = baseRoleId?.let { RoleId(it) },
        )

    fun AccountEntity.updateFrom(account: Account) {
        ownerMemberId = account.ownerMemberId.id
        cristalixNickname = account.cristalixNickname.value
        updatedAt = account.updatedAt
        status = account.status
        deactivatedAt = account.deactivatedAt
        deletedAt = account.deletedAt
        maxMembers = account.maxMembers
        baseRoleId = account.baseRoleId?.id
    }
}
