package com.mnemos.account.persistence.repository.account.role

import com.mnemos.account.domain.exception.role.RoleAssignmentNotFoundException
import com.mnemos.account.domain.model.account.AccountMemberId
import com.mnemos.account.domain.model.account.role.RoleId
import com.mnemos.account.domain.model.account.role.assignment.RoleAssignment
import com.mnemos.account.domain.repository.role.RoleAssignmentRepository
import com.mnemos.account.persistence.entity.role.RoleAssignmentEntityId
import com.mnemos.account.persistence.mapper.AccountRoleAssignmentPersistenceMapper.toDomain
import com.mnemos.account.persistence.mapper.AccountRoleAssignmentPersistenceMapper.toEntity
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
                accountMemberId = accountMemberId.id,
                roleId = roleId.id,
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
                accountMemberId = accountMemberId.id,
                roleId = roleId.id,
            )
        return jpaRepository
            .findById(id)
            .map { it.toDomain() }
            .orElse(null)
    }

    override fun findAllByMemberId(accountMemberId: AccountMemberId): List<RoleAssignment> =
        jpaRepository
            .findAllByMemberId(accountMemberId.id)
            .map { it.toDomain() }
            .toList()

    override fun findAllByRoleId(roleId: RoleId): List<RoleAssignment> =
        jpaRepository
            .findAllByRoleId(roleId.id)
            .map { it.toDomain() }
            .toList()

    override fun countByRoleId(roleId: RoleId): Int = jpaRepository.countByRoleId(roleId.id)

    override fun findMaxRankByMemberId(accountMemberId: AccountMemberId): Long =
        jpaRepository.findMaxRankByMemberId(accountMemberId.id) ?: Long.MAX_VALUE

    override fun findMinRankByMemberId(accountMemberId: AccountMemberId): Long =
        jpaRepository.findMinRanksByMemberId(accountMemberId.id) ?: Long.MAX_VALUE

    override fun exists(
        accountMemberId: AccountMemberId,
        roleId: RoleId,
    ): Boolean = jpaRepository.existsByAccountMemberIdAndRoleId(accountMemberId.id, roleId.id)
}
