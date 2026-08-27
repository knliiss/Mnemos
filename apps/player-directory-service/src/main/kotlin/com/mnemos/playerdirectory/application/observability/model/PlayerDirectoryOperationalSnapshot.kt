package com.mnemos.playerdirectory.application.observability.model

import java.time.Instant

data class PlayerDirectoryOperationalSnapshot(
    val trackingDueCount: Long,
    val oldestTrackingDueAt: Instant?,
    val resolutionQueueCount: Long,
    val trackedOnlineCount: Long,
    val trackedSaoCount: Long,
    val stalePresenceCount: Long,
)
