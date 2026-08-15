package com.mnemos.personalization.adapter.out.persistence.selection

import jakarta.persistence.LockModeType
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Lock
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import java.util.UUID

interface NotificationPackSelectionRevisionJpaRepository : JpaRepository<NotificationPackSelectionRevisionEntity, UUID> {
    @Modifying
    @Query(
        value = """
            INSERT INTO notification_pack_selection_revisions (user_id, revision)
            VALUES (:userId, 0)
            ON CONFLICT (user_id) DO NOTHING
        """,
        nativeQuery = true,
    )
    fun ensureState(userId: UUID): Int

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query(
        "SELECT state FROM NotificationPackSelectionRevisionEntity state " +
            "WHERE state.userId = :userId",
    )
    fun findByUserIdForUpdate(userId: UUID): NotificationPackSelectionRevisionEntity?
}
