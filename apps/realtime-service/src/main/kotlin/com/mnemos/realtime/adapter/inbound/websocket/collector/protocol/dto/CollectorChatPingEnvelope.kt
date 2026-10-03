package com.mnemos.realtime.adapter.inbound.websocket.collector.protocol.dto

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import java.time.Instant
import java.util.UUID

@JsonIgnoreProperties(ignoreUnknown = true)
data class CollectorChatPingEnvelope(
    val type: String,
    val messageId: UUID,
    val observedAt: Instant,
    val sender: String,
    val message: String,
    val text: String,
    val mentions: List<String>,
) {
    init {
        require(sender.length in 4..20)
        require(message.isNotBlank() && message.length <= 1024)
        require(text.length <= 512)
        require(mentions.isNotEmpty() && mentions.size <= 16)
        require(mentions.all { mention -> mention.length in 4..20 })
    }
}
