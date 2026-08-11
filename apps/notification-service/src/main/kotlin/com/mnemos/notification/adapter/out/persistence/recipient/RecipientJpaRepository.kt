package com.mnemos.notification.adapter.out.persistence.recipient

import com.mnemos.notification.adapter.out.persistence.recipient.query.RecipientViewProjection
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.util.UUID

interface RecipientJpaRepository : JpaRepository<RecipientEntity, UUID> {
    @Modifying
    @Query(
        value = """
            INSERT INTO recipients (
                user_id,
                telegram_id
            )
            VALUES (
                :userId,
                :telegramId
            )
            ON CONFLICT (user_id)
            DO UPDATE SET
                telegram_id = EXCLUDED.telegram_id
        """,
        nativeQuery = true,
    )
    fun upsert(
        userId: UUID,
        telegramId: Long,
    )

    @Query(
        """
        SELECT
            r.userId,
            r.telegramId,
            r.priority
        FROM RecipientEntity r
        WHERE r.userId IN :userIds
    """,
    )
    fun findViewsByUserIdIn(
        @Param("userIds") userIds: Set<UUID>,
    ): List<RecipientViewProjection>

    @Query(
        """
        SELECT
            r.userId,
            r.telegramId,
            r.priority
        FROM RecipientEntity r
        WHERE r.telegramId = :telegramId
        """,
    )
    fun findViewByTelegramId(
        @Param("telegramId") telegramId: Long,
    ): RecipientViewProjection?
}
