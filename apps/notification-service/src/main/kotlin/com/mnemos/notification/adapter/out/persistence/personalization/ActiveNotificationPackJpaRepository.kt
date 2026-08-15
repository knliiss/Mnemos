package com.mnemos.notification.adapter.out.persistence.personalization

import jakarta.persistence.LockModeType
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Lock
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import java.util.UUID

interface ActiveNotificationPackJpaRepository : JpaRepository<ActiveNotificationPackEntity, UUID> {
    @Modifying
    @Query(
        value = """
            INSERT INTO active_notification_packs (
                user_id,
                personalization_id,
                mixed_aggregate_title,
                templates,
                last_event_id,
                last_event_at
            )
            VALUES (
                :userId,
                NULL,
                NULL,
                NULL,
                CAST('00000000-0000-0000-0000-000000000000' AS UUID),
                TIMESTAMPTZ '1970-01-01 00:00:00+00'
            )
            ON CONFLICT (user_id) DO NOTHING
        """,
        nativeQuery = true,
    )
    fun ensureState(userId: UUID): Int

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT state FROM ActiveNotificationPackEntity state WHERE state.userId = :userId")
    fun findByUserIdForUpdate(userId: UUID): ActiveNotificationPackEntity?
}
