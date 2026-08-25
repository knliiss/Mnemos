package com.mnemos.account.infrastructure.persistence.repository.identity

import com.mnemos.account.infrastructure.persistence.projection.identity.AccountIdentityDirectoryProjection
import com.mnemos.account.infrastructure.persistence.projection.identity.IdentitySearchProjection
import com.mnemos.account.infrastructure.persistence.projection.identity.UserIdentityDirectoryProjection
import com.mnemos.account.persistence.entity.user.UserEntity
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.util.UUID

interface JpaIdentitySearchRepository : JpaRepository<UserEntity, UUID> {
    @Query(
        value = """
            WITH matched_users AS (
                SELECT
                    u.id,
                    MIN(
                        CASE
                            WHEN CAST(u.id AS text) = lower(:query) THEN 0
                            WHEN lower(u.username) = lower(:query) THEN 1
                            WHEN lower(a.cristalix_nickname) = lower(:query) THEN 2
                            WHEN CAST(u.telegram_id AS text) = :query THEN 3
                            WHEN CAST(a.cristalix_uuid AS text) = lower(:query) THEN 4
                            ELSE 5
                        END
                    ) AS match_rank
                FROM users u
                LEFT JOIN account_members am ON am.user_id = u.id
                LEFT JOIN accounts a ON a.id = am.account_id
                WHERE CAST(u.id AS text) = lower(:query)
                   OR lower(u.username) LIKE lower(concat('%', :query, '%'))
                   OR lower(a.cristalix_nickname) LIKE lower(concat('%', :query, '%'))
                   OR CAST(u.telegram_id AS text) = :query
                   OR CAST(a.cristalix_uuid AS text) = lower(:query)
                GROUP BY u.id, u.username
                ORDER BY match_rank, lower(u.username), u.id
                LIMIT :limit
            )
            SELECT
                u.id AS "userId",
                u.username AS "username",
                u.telegram_id AS "telegramId",
                a.id AS "accountId",
                a.cristalix_uuid AS "cristalixUuid",
                a.cristalix_nickname AS "cristalixNickname",
                (a.status = 'ACTIVE') AS "active"
            FROM matched_users matched
            JOIN users u ON u.id = matched.id
            LEFT JOIN account_members am ON am.user_id = u.id
            LEFT JOIN accounts a ON a.id = am.account_id
            ORDER BY
                matched.match_rank,
                lower(u.username),
                u.id,
                lower(a.cristalix_nickname) NULLS LAST,
                a.id NULLS LAST
        """,
        nativeQuery = true,
    )
    fun search(
        @Param("query") query: String,
        @Param("limit") limit: Int,
    ): List<IdentitySearchProjection>

    @Query(
        value = """
            SELECT
                u.id AS "userId",
                u.username AS "username",
                u.telegram_id AS "telegramId",
                u.active_account_id AS "activeAccountId",
                (
                    SELECT COUNT(*)
                    FROM account_members account_member_count
                    WHERE account_member_count.user_id = u.id
                ) AS "accountCount",
                u.created_at AS "createdAt"
            FROM users u
            WHERE :query = ''
               OR CAST(u.id AS text) = lower(:query)
               OR lower(u.username) LIKE lower(concat('%', :query, '%'))
               OR CAST(u.telegram_id AS text) = :query
            ORDER BY
                CASE WHEN :orderName = 'OLDEST' THEN u.created_at END ASC,
                CASE WHEN :orderName = 'NEWEST' THEN u.created_at END DESC,
                u.id ASC
        """,
        countQuery = """
            SELECT COUNT(*)
            FROM users u
            WHERE :query = ''
               OR CAST(u.id AS text) = lower(:query)
               OR lower(u.username) LIKE lower(concat('%', :query, '%'))
               OR CAST(u.telegram_id AS text) = :query
        """,
        nativeQuery = true,
    )
    fun searchUsersDirectory(
        @Param("query") query: String,
        @Param("orderName") orderName: String,
        pageable: Pageable,
    ): Page<UserIdentityDirectoryProjection>

    @Query(
        value = """
            SELECT
                account.id AS "accountId",
                account.cristalix_uuid AS "cristalixUuid",
                account.cristalix_nickname AS "cristalixNickname",
                account.status AS "status",
                owner_user.id AS "ownerUserId",
                owner_user.username AS "ownerUsername",
                account.created_at AS "createdAt"
            FROM accounts account
            JOIN account_members owner_member
              ON owner_member.id = account.owner_member_id
            JOIN users owner_user
              ON owner_user.id = owner_member.user_id
            WHERE :query = ''
               OR CAST(account.id AS text) = lower(:query)
               OR CAST(account.cristalix_uuid AS text) = lower(:query)
               OR lower(account.cristalix_nickname) LIKE lower(concat('%', :query, '%'))
            ORDER BY
                CASE WHEN :orderName = 'OLDEST' THEN account.created_at END ASC,
                CASE WHEN :orderName = 'NEWEST' THEN account.created_at END DESC,
                account.id ASC
        """,
        countQuery = """
            SELECT COUNT(*)
            FROM accounts account
            WHERE :query = ''
               OR CAST(account.id AS text) = lower(:query)
               OR CAST(account.cristalix_uuid AS text) = lower(:query)
               OR lower(account.cristalix_nickname) LIKE lower(concat('%', :query, '%'))
        """,
        nativeQuery = true,
    )
    fun searchAccountsDirectory(
        @Param("query") query: String,
        @Param("orderName") orderName: String,
        pageable: Pageable,
    ): Page<AccountIdentityDirectoryProjection>
}
