package com.mnemos.realtime.application.collector.port.out

import com.mnemos.realtime.domain.collector.model.CollectorCredentialId
import java.time.Instant
import java.util.UUID
import java.util.concurrent.CompletionStage

interface CollectorIngressPublisher {
    fun publishReport(
        ingressId: UUID,
        collectorId: CollectorCredentialId,
        rawMessage: String,
        activeCollectors: Int,
        receivedAt: Instant,
    ): CompletionStage<Unit>

    fun publishChatPing(
        ingressId: UUID,
        collectorId: CollectorCredentialId,
        messageId: UUID,
        observedAt: Instant,
        sender: String,
        message: String,
        text: String,
        mentions: List<String>,
        activeCollectors: Int,
        receivedAt: Instant,
    ): CompletionStage<Unit>

    fun publishPresence(
        activeCollectors: Int,
        revision: Long,
        occurredAt: Instant,
    ): CompletionStage<Unit>
}
