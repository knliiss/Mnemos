package com.mnemos.accountservice.persistence.repository.account.role

import com.mnemos.accountservice.domain.exception.role.RoleAssignmentNotFoundException
import com.mnemos.accountservice.domain.model.account.AccountMemberId
import com.mnemos.accountservice.domain.model.account.role.RoleId
import com.mnemos.accountservice.domain.model.account.role.assignment.RoleAssignment
import com.mnemos.accountservice.domain.repository.role.RoleAssignmentRepository
import com.mnemos.accountservice.persistence.entity.role.RoleAssignmentEntityId
import com.mnemos.accountservice.persistence.mapper.AccountRoleAssignmentPersistenceMapper.toDomain
import com.mnemos.accountservice.persistence.mapper.AccountRoleAssignmentPersistenceMapper.toEntity
import org.springframework.stereotype.Repository

@Repository
class RoleAssignmentRepositoryAdapter(
    private val jpaRepository: JpaRoleAssignmentRepository,
) : RoleAssignmentRepository {
    override fun create(assignment: RoleAssignment): RoleAssignment = jpaRepository.save(assignment.toEntity()).toDomain()

    override fun remove(
        roleId: RoleId,
        accountMemberId: AccountMemberId,
    ): Boolean {
        val id =
            RoleAssignmentEntityId(
                accountMemberId = accountMemberId.value,
                roleId = roleId.value,
            )
        if (!jpaRepository.existsById(id)) {
            throw RoleAssignmentNotFoundException(
                memberId = accountMemberId,
                roleId = roleId,
            )
        }

        jpaRepository.deleteById(id)
        return true
    }

    override fun find(
        roleId: RoleId,
        accountMemberId: AccountMemberId,
    ): RoleAssignment? {
        val id =
            RoleAssignmentEntityId(
                accountMemberId = accountMemberId.value,
                roleId = roleId.value,
            )
        return jpaRepository
            .findById(id)
            .map { it.toDomain() }
            .orElse(null)
    }

    override fun findAllByMemberId(accountMemberId: AccountMemberId): List<RoleAssignment> =
        jpaRepository
            .findAllByMemberId(accountMemberId.value)
            .map { it.toDomain() }
            .toList()

    override fun findAllByRoleId(roleId: RoleId): List<RoleAssignment> =
        jpaRepository
            .findAllByRoleId(roleId.value)
            .map { it.toDomain() }
            .toList()

    override fun countByRoleId(roleId: RoleId): Int = jpaRepository.countByRoleId(roleId.value)

    override fun findMaxRankByMemberId(accountMemberId: AccountMemberId): Long =
        jpaRepository.findMaxRankByMemberId(accountMemberId.value) ?: Long.MAX_VALUE

    override fun findMinRankByMemberId(accountMemberId: AccountMemberId): Long =
        jpaRepository.findMinRanksByMemberId(accountMemberId.value) ?: Long.MAX_VALUE

    override fun exists(
        accountMemberId: AccountMemberId,
        roleId: RoleId,
    ): Boolean = jpaRepository.existsByAccountMemberIdAndRoleId(accountMemberId.value, roleId.value)
}
