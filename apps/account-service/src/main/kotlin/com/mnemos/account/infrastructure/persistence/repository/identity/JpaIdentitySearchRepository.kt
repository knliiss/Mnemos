package com.mnemos.account.infrastructure.persistence.repository.identity

import com.mnemos.account.infrastructure.persistence.projection.identity.IdentitySearchProjection
import com.mnemos.account.persistence.entity.user.UserEntity
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
                            WHEN lower(u.username) = lower(:query) THEN 0
                            WHEN lower(a.cristalix_nickname) = lower(:query) THEN 1
                            WHEN CAST(u.telegram_id AS text) = :query THEN 2
                            WHEN CAST(a.cristalix_uuid AS text) = lower(:query) THEN 3
                            ELSE 4
                        END
                    ) AS match_rank
                FROM users u
                LEFT JOIN account_members am ON am.user_id = u.id
                LEFT JOIN accounts a ON a.id = am.account_id
                WHERE lower(u.username) LIKE lower(concat('%', :query, '%'))
                   OR lower(a.cristalix_nickname) LIKE lower(concat('%', :query, '%'))
                   OR CAST(u.telegram_id AS text) = :query
                   OR CAST(a.cristalix_uuid AS text) = lower(:query)
                GROUP BY u.id, u.username
                ORDER BY match_rank, lower(u.username)
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
            ORDER BY matched.match_rank, lower(u.username), lower(a.cristalix_nickname)
        """,
        nativeQuery = true,
    )
    fun search(
        @Param("query") query: String,
        @Param("limit") limit: Int,
    ): List<IdentitySearchProjection>
}
