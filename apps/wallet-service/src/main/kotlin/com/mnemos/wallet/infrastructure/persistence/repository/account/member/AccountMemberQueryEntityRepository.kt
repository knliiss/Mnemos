package com.mnemos.accountservice.infrastructure.persistence.repository.account.member

import com.mnemos.accountservice.infrastructure.persistence.projection.account.member.AccountMemberListProjection
import com.mnemos.accountservice.persistence.entity.account.AccountMemberEntity
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.Repository
import org.springframework.data.repository.query.Param
import java.util.UUID

interface AccountMemberQueryEntityRepository : Repository<AccountMemberEntity, UUID> {
    @Query(
        """
        SELECT member.id AS "id",
            user_account.username AS "name",
            member.id = account.owner_member_id AS "owner",
            member.joined_at AS "joinedAt",
            highest_role.id AS "highestRoleId",
            highest_role.role_name AS "highestRoleName",
            highest_role.role_color AS "highestRoleColor",
            highest_role.hierarchy_rank AS "highestRoleRank"
        FROM account_members member
        JOIN accounts account
            ON account.id = member.account_id
        JOIN users user_account
            ON user_account.id = member.user_id
        LEFT JOIN LATERAL (
            SELECT role.id,
                role.role_name,
                role.role_color,
                role.hierarchy_rank
            FROM role_assignments assignment
            JOIN roles role
                ON role.id = assignment.role_id
                AND role.account_id = assignment.account_id
            WHERE assignment.account_id = member.account_id
                AND assignment.account_member_id = member.id
            ORDER BY role.hierarchy_rank ASC
            LIMIT 1
        ) highest_role ON TRUE
        WHERE member.account_id = :accountId
        ORDER BY
            CASE
                WHEN member.id = account.owner_member_id THEN 0
                ELSE 1
            END,
            highest_role.hierarchy_rank ASC NULLS LAST,
            user_account.username ASC
        """,
        nativeQuery = true,
    )
    fun findAllByAccountId(
        @Param("accountId") accountId: UUID,
    ): List<AccountMemberListProjection>
}
