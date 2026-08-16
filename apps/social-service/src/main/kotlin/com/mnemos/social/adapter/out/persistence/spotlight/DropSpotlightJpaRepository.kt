package com.mnemos.social.adapter.out.persistence.spotlight

import com.mnemos.social.domain.spotlight.model.DropSpotlightStatus
import jakarta.persistence.LockModeType
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Lock
import java.time.Instant
import java.util.UUID

interface DropSpotlightJpaRepository : JpaRepository<DropSpotlightEntity, UUID> {
    fun existsByItemDropEventId(itemDropEventId: UUID): Boolean

    fun findTopByBuyerUserIdOrderByCreatedAtDesc(buyerUserId: UUID): DropSpotlightEntity?

    fun findTopByStatusInOrderByScheduledAtDesc(statuses: Collection<DropSpotlightStatus>): DropSpotlightEntity?

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    fun findTop20ByStatusAndScheduledAtLessThanEqualOrderByScheduledAtAsc(
        status: DropSpotlightStatus,
        scheduledAt: Instant,
    ): List<DropSpotlightEntity>
}
