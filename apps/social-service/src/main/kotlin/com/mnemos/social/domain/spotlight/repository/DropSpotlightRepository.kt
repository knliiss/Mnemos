package com.mnemos.social.domain.spotlight.repository

import com.mnemos.social.domain.spotlight.model.DropSpotlight
import com.mnemos.social.domain.spotlight.model.DropSpotlightId
import java.time.Instant
import java.util.UUID

interface DropSpotlightRepository {
    fun save(spotlight: DropSpotlight): DropSpotlight

    fun findById(id: DropSpotlightId): DropSpotlight?

    fun findByIdForUpdate(id: DropSpotlightId): DropSpotlight?

    fun existsNonFailedByItemDropEventId(itemDropEventId: UUID): Boolean

    fun findLatestByBuyerUserId(userId: UUID): DropSpotlight?

    fun findNextDue(now: Instant): DropSpotlight?

    fun findLatestPublishedAt(): Instant?
}
