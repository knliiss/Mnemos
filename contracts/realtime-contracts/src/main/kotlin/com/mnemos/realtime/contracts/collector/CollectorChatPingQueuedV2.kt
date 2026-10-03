package com.mnemos.realtime.contracts.collector

import java.time.Instant
import java.util.UUID

data class CollectorChatPingQueuedV2(
    val ingressId: UUID,
    val collectorId: UUID,
    val messageId: UUID,
    val observedAt: Instant,
    val sender: String,
    val message: String,
    val text: String,
    val mentions: List<String>,
    val activeCollectors: Int,
    val receivedAt: Instant,
) {
    init {
        require(sender.isNotBlank())
        require(message.isNotBlank())
        require(mentions.isNotEmpty())
        require(activeCollectors > 0)
    }
}
