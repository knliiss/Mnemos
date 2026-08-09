package com.mnemos.account.persistence.repository.account.role

import com.mnemos.account.domain.exception.role.RoleNotFoundException
import com.mnemos.account.domain.model.account.AccountMemberId
import com.mnemos.account.domain.model.account.role.AccountRoleName
import com.mnemos.account.domain.model.account.role.Role
import com.mnemos.account.domain.model.account.role.RoleId
import com.mnemos.account.domain.repository.role.RoleRepository
import com.mnemos.account.persistence.mapper.AccountRolePersistenceMapper.toDomain
import com.mnemos.account.persistence.mapper.AccountRolePersistenceMapper.toEntity
import com.mnemos.account.persistence.mapper.AccountRolePersistenceMapper.updateFrom
import com.mnemos.common.core.model.id.AccountId
import org.springframework.stereotype.Repository

@Repository
class RoleRepositoryAdapter(
    private val jpaRoleRepository: JpaRoleRepository,
) : RoleRepository {
    override fun create(role: Role): Role = jpaRoleRepository.save(role.toEntity()).toDomain()

    override fun update(role: Role): Role {
        val entity =
            jpaRoleRepository
                .findById(role.id.id)
                .orElseThrow { RoleNotFoundException(roleId = role.id, accountId = role.accountId) }
        entity.updateFrom(role)

        return jpaRoleRepository.save(entity).toDomain()
    }

    override fun deleteById(id: RoleId) {
        jpaRoleRepository.deleteById(id.id)
    }

    override fun findById(id: RoleId): Role? =
        jpaRoleRepository
            .findById(id.id)
            .map { it.toDomain() }
            .orElse(null)

    override fun existsByAccountIdAndRoleName(
        accountId: AccountId,
        name: AccountRoleName,
    ): Boolean =
        jpaRoleRepository.existsByAccountIdAndRoleName(
            accountId = accountId.id,
            roleName = name.value,
        )

    override fun findByIdAndAccountId(
        roleId: RoleId,
        accountId: AccountId,
    ): Role? = jpaRoleRepository.findByIdAndAccountId(roleId.id, accountId.id)?.toDomain()

    override fun findMinRank(accountId: AccountId): Long = jpaRoleRepository.findMinRank(accountId.id) ?: 0L

    override fun findMaxRank(accountId: AccountId): Long = jpaRoleRepository.findMaxRank(accountId.id) ?: Long.MAX_VALUE

    override fun findAllByAccountIdOrderByRank(accountId: AccountId): List<Role> =
        jpaRoleRepository
            .findAllByAccountIdOrderByHierarchyRank(accountId.id)
            .map { it.toDomain() }
            .toList()

    override fun updateAll(roles: Collection<Role>): List<Role> =
        jpaRoleRepository
            .saveAll(roles.map { it.toEntity() })
            .map { it.toDomain() }
            .toList()

    override fun findAllByMemberIdAndAccountIdOrderByRank(
        memberId: AccountMemberId,
        accountId: AccountId,
    ): List<Role> =
        jpaRoleRepository
            .findAllByMemberIdAndAccountIdOrderByRank(
                memberId = memberId.id,
                accountId = accountId.id,
            ).map { it.toDomain() }

    override fun findAllByAccountId(accountId: AccountId): List<Role> =
        jpaRoleRepository
            .findAllByAccountIdOrderByHierarchyRank(accountId = accountId.id)
            .map { it.toDomain() }

    override fun findAllByMemberId(
        accountId: AccountId,
        memberId: AccountMemberId,
    ): List<Role> =
        jpaRoleRepository
            .findAllByMemberIdAndAccountIdOrderByRank(
                memberId = memberId.id,
                accountId = accountId.id,
            ).map { it.toDomain() }
}
