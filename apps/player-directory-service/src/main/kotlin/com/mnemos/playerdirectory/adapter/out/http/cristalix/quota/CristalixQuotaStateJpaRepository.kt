package com.mnemos.playerdirectory.adapter.out.http.cristalix.quota

import com.mnemos.playerdirectory.adapter.out.http.cristalix.CristalixCredentialLane
import jakarta.persistence.LockModeType
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Lock
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.time.Instant

interface CristalixQuotaStateJpaRepository : JpaRepository<CristalixQuotaStateEntity, CristalixCredentialLane> {
    @Modifying
    @Query(
        value = """
            INSERT INTO cristalix_quota_states (
                lane,
                available_tokens,
                last_refill_at,
                cooldown_until
            )
            VALUES (
                :lane,
                :capacity,
                :now,
                NULL
            )
            ON CONFLICT (lane) DO NOTHING
        """,
        nativeQuery = true,
    )
    fun insertIfAbsent(
        @Param("lane") lane: String,
        @Param("capacity") capacity: Double,
        @Param("now") now: Instant,
    ): Int

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT state FROM CristalixQuotaStateEntity state WHERE state.lane = :lane")
    fun findByLaneForUpdate(
        @Param("lane") lane: CristalixCredentialLane,
    ): CristalixQuotaStateEntity?
}
