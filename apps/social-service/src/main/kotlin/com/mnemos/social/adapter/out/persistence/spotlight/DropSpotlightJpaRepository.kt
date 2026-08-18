package com.mnemos.social.adapter.out.persistence.spotlight

import com.mnemos.social.domain.spotlight.model.DropSpotlightStatus
import jakarta.persistence.LockModeType
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Lock
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.time.Instant
import java.util.UUID

interface DropSpotlightJpaRepository : JpaRepository<DropSpotlightEntity, UUID> {
    fun findByIdempotencyKey(idempotencyKey: UUID): DropSpotlightEntity?

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select spotlight from DropSpotlightEntity spotlight where spotlight.id = :spotlightId")
    fun findByIdForUpdate(
        @Param("spotlightId") spotlightId: UUID,
    ): DropSpotlightEntity?

    fun existsByItemDropEventIdAndStatusNot(
        itemDropEventId: UUID,
        status: DropSpotlightStatus,
    ): Boolean

    fun findTopByBuyerUserIdOrderByCreatedAtDesc(buyerUserId: UUID): DropSpotlightEntity?

    fun findTopByStatusInOrderByScheduledAtDesc(statuses: Collection<DropSpotlightStatus>): DropSpotlightEntity?

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    fun findFirstByStatusAndScheduledAtLessThanEqualOrderBySequenceAsc(
        status: DropSpotlightStatus,
        scheduledAt: Instant,
    ): DropSpotlightEntity?

    fun findTopByStatusAndPublishedAtIsNotNullOrderByPublishedAtDesc(status: DropSpotlightStatus): DropSpotlightEntity?
}
