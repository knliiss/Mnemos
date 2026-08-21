package com.mnemos.account.persistence.repository.account.role

import com.mnemos.account.persistence.entity.role.RoleEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import java.util.UUID

interface JpaRoleRepository : JpaRepository<RoleEntity, UUID> {
    fun existsByAccountIdAndRoleName(
        accountId: UUID,
        roleName: String,
    ): Boolean

    fun findByIdAndAccountId(
        id: UUID,
        accountId: UUID,
    ): RoleEntity?

    @Query(
        """
            SELECT MAX(role.hierarchyRank)
            FROM RoleEntity role
            WHERE role.accountId = :accountId
        """,
    )
    fun findMaxRank(accountId: UUID): Long?

    fun findAllByAccountIdOrderByHierarchyRank(accountId: UUID): List<RoleEntity>

    @Query(
        """
            SELECT role
            FROM RoleEntity role
            WHERE role.accountId = :accountId
                AND EXISTS(
                SELECT assignment.id
                FROM RoleAssignmentEntity assignment
                WHERE assignment.id.accountMemberId = :memberId
                    AND assignment.id.roleId = role.id
                    AND assignment.accountId = :accountId
            )
            ORDER BY role.hierarchyRank ASC
        """,
    )
    fun findAllByMemberIdAndAccountIdOrderByRank(
        memberId: UUID,
        accountId: UUID,
    ): List<RoleEntity>
}
