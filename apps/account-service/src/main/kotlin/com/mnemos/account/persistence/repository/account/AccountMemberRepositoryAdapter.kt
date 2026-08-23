package com.mnemos.account.persistence.repository.account

import com.mnemos.account.domain.model.account.AccountMember
import com.mnemos.account.domain.model.account.AccountMemberId
import com.mnemos.account.domain.repository.account.AccountMemberRepository
import com.mnemos.account.persistence.mapper.AccountMemberPersistenceMapper.toDomain
import com.mnemos.account.persistence.mapper.AccountMemberPersistenceMapper.toEntity
import com.mnemos.common.core.model.id.AccountId
import com.mnemos.common.core.model.id.UserId
import com.mnemos.common.core.model.permission.RolePermission
import org.springframework.stereotype.Repository

@Repository
class AccountMemberRepositoryAdapter(
    private val jpaAccountMemberRepository: JpaAccountMemberRepository,
) : AccountMemberRepository {
    override fun create(member: AccountMember): AccountMember = jpaAccountMemberRepository.save(member.toEntity()).toDomain()

    override fun findByAccountIdAndUserId(
        accountId: AccountId,
        userId: UserId,
    ): AccountMember? =
        jpaAccountMemberRepository
            .findByAccountIdAndUserId(
                accountId = accountId.id,
                userId = userId.id,
            )?.toDomain()

    override fun findUserIdsByAccountId(accountId: AccountId): List<UserId> =
        jpaAccountMemberRepository
            .findUserIdsByAccountId(accountId.id)
            .map(::UserId)

    override fun existsByAccountIdAndUserId(
        accountId: AccountId,
        userId: UserId,
    ): Boolean =
        jpaAccountMemberRepository.existsByAccountIdAndUserId(
            accountId = accountId.id,
            userId = userId.id,
        )

    override fun countByAccountId(accountId: AccountId): Int = jpaAccountMemberRepository.countByAccountId(accountId = accountId.id).toInt()

    override fun hasEffectivePermission(
        permission: RolePermission,
        memberId: AccountMemberId,
    ): Boolean = jpaAccountMemberRepository.hasEffectivePermission(permission = permission.name, accountMemberId = memberId.id)

    override fun deleteById(id: AccountMemberId) {
        jpaAccountMemberRepository.deleteById(id.id)
    }

    override fun findByIdAndAccountId(
        memberId: AccountMemberId,
        accountId: AccountId,
    ): AccountMember? = jpaAccountMemberRepository.findByIdAndAccountId(memberId.id, accountId.id)?.toDomain()

    override fun findPermissions(memberId: AccountMemberId): Set<RolePermission> =
        jpaAccountMemberRepository.findAllPermissionByMemberId(memberId.id)

    override fun hasPermission(
        memberId: AccountMemberId,
        permission: RolePermission,
    ): Boolean =
        jpaAccountMemberRepository.hasPermission(
            permission = permission.name,
            memberId = memberId.id,
        )
}
