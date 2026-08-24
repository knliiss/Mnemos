package com.mnemos.account.infrastructure.persistence.repository.account

import com.mnemos.account.infrastructure.persistence.projection.account.AccountDetailsProjection
import com.mnemos.account.infrastructure.persistence.projection.account.AccountLookupSummaryProjection
import com.mnemos.account.infrastructure.persistence.projection.account.AccountSummaryProjection
import com.mnemos.account.persistence.entity.account.AccountEntity
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.Repository
import org.springframework.data.repository.query.Param
import java.util.UUID

interface AccountQueryEntityRepository : Repository<AccountEntity, UUID> {
    @Query(
        """
        SELECT account.id AS "id",
            account.cristalix_uuid AS "cristalixUuid",
            account.cristalix_nickname AS "cristalixNickname",
            current_member.id AS "currentMemberId",
            account.owner_member_id AS "ownerMemberId",
            account.max_members AS "maxMembers",
            account.status AS "status",
            (
                SELECT COUNT(*)
                FROM account_members member_count
                WHERE member_count.account_id = account.id
            ) AS "memberCount"
        FROM accounts account
        JOIN account_members current_member
            ON current_member.account_id = account.id
            AND current_member.user_id = :userId
        WHERE account.status = 'ACTIVE'
            OR current_member.id = account.owner_member_id
        ORDER BY account.created_at ASC, account.id ASC
        """,
        nativeQuery = true,
    )
    fun findSummariesByUserId(
        @Param("userId") userId: UUID,
    ): List<AccountSummaryProjection>

    @Query(
        """
        SELECT account.id AS "id",
            account.cristalix_uuid AS "cristalixUuid",
            account.cristalix_nickname AS "cristalixNickname"
        FROM accounts account
        WHERE account.id IN (:accountIds)
        ORDER BY lower(account.cristalix_nickname) ASC, account.id ASC
        """,
        nativeQuery = true,
    )
    fun findLookupSummariesByAccountIds(
        @Param("accountIds") accountIds: Set<UUID>,
    ): List<AccountLookupSummaryProjection>

    @Query(
        """
        SELECT account.id AS "id",
            account.cristalix_uuid AS "cristalixUuid",
            account.cristalix_nickname AS "cristalixNickname",
            current_member.id AS "currentMemberId",
            account.owner_member_id AS "ownerMemberId",
            account.max_members AS "maxMembers",
            account.status AS "status",
            account.base_role_id AS "baseRoleId",
            account.created_at AS "createdAt",
            account.updated_at AS "updatedAt",
            (
                SELECT COUNT(*)
                FROM account_members member_count
                WHERE member_count.account_id = account.id
            ) AS "memberCount"
        FROM accounts account
        JOIN account_members current_member
            ON current_member.account_id = account.id
            AND current_member.user_id = :userId
        WHERE account.id = :accountId
            AND account.status = 'ACTIVE'
        """,
        nativeQuery = true,
    )
    fun findDetailsByAccountIdAndUserId(
        @Param("accountId") accountId: UUID,
        @Param("userId") userId: UUID,
    ): AccountDetailsProjection?
}
