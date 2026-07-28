package com.mnemos.accountservice.persistence.repository.account.role

import com.mnemos.accountservice.persistence.entity.role.RoleAssignmentEntity
import com.mnemos.accountservice.persistence.entity.role.RoleAssignmentEntityId
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.util.UUID

interface JpaRoleAssignmentRepository : JpaRepository<RoleAssignmentEntity, RoleAssignmentEntityId> {
    @Query(
        """
    SELECT assignment
    FROM RoleAssignmentEntity assignment
    WHERE assignment.id.accountMemberId = :accountMemberId
    """,
    )
    fun findAllByMemberId(
        @Param("accountMemberId") accountMemberId: UUID,
    ): List<RoleAssignmentEntity>

    @Query(
        """
            SELECT assignment
            FROM RoleAssignmentEntity assignment
            WHERE assignment.id.roleId = :roleId
        """,
    )
    fun findAllByRoleId(
        @Param("roleId") roleId: UUID,
    ): List<RoleAssignmentEntity>

    @Query(
        """
            SELECT count(*)
            FROM RoleAssignmentEntity assignment
            WHERE assignment.id.roleId = :roleId
        """,
    )
    fun countByRoleId(roleId: UUID): Int

    @Query(
        """
            SELECT COALESCE(MAX(role.hierarchyRank))
            FROM RoleAssignmentEntity assignment
            JOIN RoleEntity role ON assignment.id.roleId = role.id
            WHERE assignment.id.accountMemberId = :memberId
        """,
    )
    fun findMaxRankByMemberId(memberId: UUID): Long?

    @Query(
        """
            SELECT COALESCE(MIN(role.hierarchyRank))
            FROM RoleAssignmentEntity assignment
            JOIN RoleEntity role ON assignment.id.roleId = role.id
            WHERE assignment.id.accountMemberId = :memberId
        """,
    )
    fun findMinRanksByMemberId(memberId: UUID): Long?

    @Query(
        """
            SELECT 1
            FROM RoleAssignmentEntity assignment
            WHERE assignment.id.roleId = :roleId
            AND assignment.id.accountMemberId = :accountMemberId
        """,
    )
    fun existsByAccountMemberIdAndRoleId(
        accountMemberId: UUID,
        roleId: UUID,
    ): Boolean
}
