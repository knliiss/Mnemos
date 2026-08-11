package com.mnemos.notification.adapter.out.persistence.subscription.query

import java.time.Instant
import java.util.UUID

interface SubscriptionViewProjection {
    val id: UUID
    val type: String
    val createdAt: Instant
    val droppedFor: String?
    val itemName: String?
    val boosterType: String?
    val dungeonType: String?
    val globalEventType: String?
    val raidLocations: String?
}
