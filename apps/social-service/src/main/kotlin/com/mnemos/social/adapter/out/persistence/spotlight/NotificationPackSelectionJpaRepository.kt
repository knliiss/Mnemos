package com.mnemos.social.adapter.out.persistence.spotlight

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.time.Instant
import java.util.UUID

interface NotificationPackSelectionJpaRepository : JpaRepository<NotificationPackSelectionEntity, UUID> {
    @Modifying
    @Query(
        value = """
            INSERT INTO notification_pack_selections (
                user_id,
                personalization_id,
                selection_revision,
                snapshot_json,
                occurred_at
            )
            VALUES (
                :userId,
                :personalizationId,
                :revision,
                :snapshotJson,
                :occurredAt
            )
            ON CONFLICT (user_id) DO UPDATE
            SET personalization_id = EXCLUDED.personalization_id,
                selection_revision = EXCLUDED.selection_revision,
                snapshot_json = EXCLUDED.snapshot_json,
                occurred_at = EXCLUDED.occurred_at
            WHERE notification_pack_selections.selection_revision < EXCLUDED.selection_revision
        """,
        nativeQuery = true,
    )
    fun applyIfNewer(
        @Param("userId") userId: UUID,
        @Param("personalizationId") personalizationId: UUID?,
        @Param("revision") revision: Long,
        @Param("snapshotJson") snapshotJson: String?,
        @Param("occurredAt") occurredAt: Instant,
    ): Int
}
