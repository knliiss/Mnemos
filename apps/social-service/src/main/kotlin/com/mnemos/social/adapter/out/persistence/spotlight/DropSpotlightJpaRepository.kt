package com.mnemos.social.adapter.out.persistence.spotlight

import com.mnemos.social.domain.spotlight.model.DropSpotlightStatus
import org.springframework.data.jpa.repository.JpaRepository
import java.time.Instant
import java.util.UUID

interface DropSpotlightJpaRepository : JpaRepository<DropSpotlightEntity, UUID> {
    fun existsByItemDropEventId(itemDropEventId: UUID): Boolean

    fun findTopByBuyerUserIdOrderByCreatedAtDesc(buyerUserId: UUID): DropSpotlightEntity?

    fun findTop20ByStatusAndScheduledAtLessThanEqualOrderByScheduledAtAsc(
        status: DropSpotlightStatus,
        scheduledAt: Instant,
    ): List<DropSpotlightEntity>
}
