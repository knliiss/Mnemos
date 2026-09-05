package com.mnemos.event.adapter.out.persistence.feed

import java.time.Instant
import java.util.UUID

interface EventFeedProjection {
    fun getEventType(): String

    fun getEventId(): UUID

    fun getOccurredAt(): Instant

    fun getItemId(): UUID?

    fun getItemKey(): String?

    fun getItemName(): String?

    fun getItemRarity(): String?

    fun getItemType(): String?

    fun getDroppedFor(): String?

    fun getBoosterType(): String?

    fun getActivatedBy(): String?

    fun getGlobalType(): String?

    fun getRaidLocations(): String?
}
