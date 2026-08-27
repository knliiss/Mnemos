package com.mnemos.playerdirectory.adapter.out.persistence.observability

import java.time.Instant

interface PlayerDirectoryOperationalSnapshotProjection {
    val trackingDueCount: Long
    val oldestTrackingDueAt: Instant?
    val resolutionQueueCount: Long
    val trackedOnlineCount: Long
    val trackedSaoCount: Long
    val stalePresenceCount: Long
}
