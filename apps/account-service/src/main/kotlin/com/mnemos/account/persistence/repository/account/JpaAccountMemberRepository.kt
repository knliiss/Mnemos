package com.mnemos.account.persistence.repository.account

import com.mnemos.account.persistence.entity.account.AccountMemberEntity
import com.mnemos.common.core.model.permission.RolePermission
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.util.UUID

interface JpaAccountMemberRepository : JpaRepository<AccountMemberEntity, UUID> {
    fun findByAccountIdAndUserId(
        accountId: UUID,
        userId: UUID,
    ): AccountMemberEntity?

    fun existsByAccountIdAndUserId(
        accountId: UUID,
        userId: UUID,
    ): Boolean

    fun countByAccountId(accountId: UUID): Long

    @Query(
        value = """
        SELECT EXISTS (
            SELECT 1
            FROM account_members member
            JOIN accounts account
                ON account.id = member.account_id
            WHERE member.id = :accountMemberId
              AND (
                  account.owner_member_id = member.id
                  OR EXISTS (
                      SELECT 1
                      FROM role_permissions permission
                      WHERE permission.role_id = account.base_role_id
                        AND permission.permission = :permission
                  )
                  OR EXISTS (
                      SELECT 1
                      FROM role_assignments assignment
                      JOIN role_permissions permission
                          ON permission.role_id = assignment.role_id
                      WHERE assignment.account_member_id = member.id
                        AND assignment.account_id = member.account_id
                        AND permission.permission = :permission
                  )
              )
        )
    """,
        nativeQuery = true,
    )
    fun hasEffectivePermission(
        @Param("accountMemberId") accountMemberId: UUID,
        @Param("permission") permission: String,
    ): Boolean

    fun findByIdAndAccountId(
        id: UUID,
        accountId: UUID,
    ): AccountMemberEntity?

    @Query(
        value = """
        SELECT DISTINCT permission.permission
        FROM account_members member
        JOIN accounts account
            ON account.id = member.account_id
        JOIN role_permissions permission
            ON permission.role_id = account.base_role_id
        WHERE member.id = :memberId

        UNION

        SELECT DISTINCT permission.permission
        FROM role_assignments assignment
        JOIN role_permissions permission
            ON permission.role_id = assignment.role_id
        WHERE assignment.account_member_id = :memberId
    """,
        nativeQuery = true,
    )
    fun findAllPermissionByMemberId(memberId: UUID): Set<RolePermission>

    @Query(
        value = """
        SELECT EXISTS (
            SELECT 1
            FROM account_members member
            JOIN accounts account
                ON account.id = member.account_id
            WHERE member.id = :memberId
              AND (
                  EXISTS (
                      SELECT 1
                      FROM role_permissions permission
                      WHERE permission.role_id = account.base_role_id
                        AND permission.permission = :permission
                  )
                  OR EXISTS (
                      SELECT 1
                      FROM role_assignments assignment
                      JOIN role_permissions permission
                          ON permission.role_id = assignment.role_id
                      WHERE assignment.account_member_id = member.id
                        AND assignment.account_id = member.account_id
                        AND permission.permission = :permission
                  )
              )
        )
    """,
        nativeQuery = true,
    )
    fun hasPermission(
        @Param("memberId") memberId: UUID,
        @Param("permission") permission: String,
    ): Boolean
}
