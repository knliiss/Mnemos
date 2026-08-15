package com.mnemos.personalization.adapter.out.persistence.personalization

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import java.time.Instant
import java.util.UUID

interface CustomNotificationPackFreeGrantJpaRepository :
    JpaRepository<CustomNotificationPackFreeGrantEntity, UUID> {
    @Modifying
    @Query(
        value = """
            INSERT INTO custom_notification_pack_free_grants (user_id, claimed_at)
            VALUES (:userId, :claimedAt)
            ON CONFLICT (user_id) DO NOTHING
        """,
        nativeQuery = true,
    )
    fun claim(
        userId: UUID,
        claimedAt: Instant,
    ): Int
}
