package com.mnemos.accountservice.persistence.repository.account.role

import com.mnemos.accountservice.domain.exception.role.RoleNotFoundException
import com.mnemos.accountservice.domain.model.account.AccountMemberId
import com.mnemos.accountservice.domain.model.account.role.AccountRoleName
import com.mnemos.accountservice.domain.model.account.role.Role
import com.mnemos.accountservice.domain.model.account.role.RoleId
import com.mnemos.accountservice.domain.repository.role.RoleRepository
import com.mnemos.accountservice.persistence.mapper.AccountRolePersistenceMapper.toDomain
import com.mnemos.accountservice.persistence.mapper.AccountRolePersistenceMapper.toEntity
import com.mnemos.accountservice.persistence.mapper.AccountRolePersistenceMapper.updateFrom
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
                .findById(role.id.value)
                .orElseThrow { RoleNotFoundException(roleId = role.id, accountId = role.accountId) }
        entity.updateFrom(role)

        return jpaRoleRepository.save(entity).toDomain()
    }

    override fun deleteById(id: RoleId) {
        jpaRoleRepository.deleteById(id.value)
    }

    override fun findById(id: RoleId): Role? =
        jpaRoleRepository
            .findById(id.value)
            .map { it.toDomain() }
            .orElse(null)

    override fun existsByAccountIdAndRoleName(
        accountId: AccountId,
        name: AccountRoleName,
    ): Boolean =
        jpaRoleRepository.existsByAccountIdAndRoleName(
            accountId = accountId.value,
            roleName = name.value,
        )

    override fun findByIdAndAccountId(
        roleId: RoleId,
        accountId: AccountId,
    ): Role? = jpaRoleRepository.findByIdAndAccountId(roleId.value, accountId.value)?.toDomain()

    override fun findMinRank(accountId: AccountId): Long = jpaRoleRepository.findMinRank(accountId.value) ?: 0L

    override fun findMaxRank(accountId: AccountId): Long = jpaRoleRepository.findMaxRank(accountId.value) ?: Long.MAX_VALUE

    override fun findAllByAccountIdOrderByRank(accountId: AccountId): List<Role> =
        jpaRoleRepository
            .findAllByAccountIdOrderByHierarchyRank(accountId.value)
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
                memberId = memberId.value,
                accountId = accountId.value,
            ).map { it.toDomain() }

    override fun findAllByAccountId(accountId: AccountId): List<Role> =
        jpaRoleRepository
            .findAllByAccountIdOrderByHierarchyRank(accountId = accountId.value)
            .map { it.toDomain() }

    override fun findAllByMemberId(
        accountId: AccountId,
        memberId: AccountMemberId,
    ): List<Role> =
        jpaRoleRepository
            .findAllByMemberIdAndAccountIdOrderByRank(
                memberId = memberId.value,
                accountId = accountId.value,
            ).map { it.toDomain() }
}
