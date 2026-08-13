package com.mnemos.notification.adapter.out.persistence.recipient

import com.mnemos.notification.adapter.out.persistence.recipient.query.RecipientViewProjection
import com.mnemos.notification.domain.recipient.model.RecipientDeliveryStatus
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
                telegram_id,
                priority,
                delivery_status
            )
            VALUES (
                :userId,
                :telegramId,
                'STANDARD',
                'ACTIVE'
            )
            ON CONFLICT (user_id)
            DO UPDATE SET
                telegram_id = EXCLUDED.telegram_id,
                delivery_status = 'ACTIVE'
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
          AND r.deliveryStatus =
              com.mnemos.notification.domain.recipient.model.RecipientDeliveryStatus.ACTIVE
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
          AND r.deliveryStatus =
              com.mnemos.notification.domain.recipient.model.RecipientDeliveryStatus.ACTIVE
        """,
    )
    fun findActiveViewByTelegramId(
        @Param("telegramId") telegramId: Long,
    ): RecipientViewProjection?

    @Modifying
    @Query(
        """
        UPDATE RecipientEntity recipient
        SET recipient.deliveryStatus = :status
        WHERE recipient.telegramId = :telegramId
        """,
    )
    fun updateDeliveryStatus(
        @Param("telegramId") telegramId: Long,
        @Param("status") status: RecipientDeliveryStatus,
    )
}
