package com.mnemos.social.domain.spotlight.repository

import com.mnemos.social.domain.spotlight.model.DropSpotlight
import com.mnemos.social.domain.spotlight.model.DropSpotlightId
import java.time.Instant
import java.util.UUID

interface DropSpotlightRepository {
    fun save(spotlight: DropSpotlight): DropSpotlight

    fun findById(id: DropSpotlightId): DropSpotlight?

    fun findByIdForUpdate(id: DropSpotlightId): DropSpotlight?

    fun existsByItemDropEventId(itemDropEventId: UUID): Boolean

    fun findLatestByBuyerUserId(userId: UUID): DropSpotlight?

    fun findDue(now: Instant): List<DropSpotlight>
}
