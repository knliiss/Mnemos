package com.mnemos.realtime.adapter.sse.connection

import com.mnemos.realtime.application.broadcast.model.DropSpotlightBroadcastPayload
import com.mnemos.realtime.application.broadcast.model.DropSpotlightPresentationMode
import com.mnemos.realtime.application.broadcast.model.RealtimeBroadcast
import com.mnemos.realtime.configuration.delivery.RealtimeDeliveryProperties
import com.mnemos.realtime.support.RecordingRealtimeEventSink
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter
import java.time.Instant
import java.util.UUID

class SseConnectionRegistryBroadcastTest {
    @Test
    fun `merges replay and live broadcast without sending duplicate`() {
        val registry = registry()
        val emitter = mock<SseEmitter>()
        val userId = UUID.fromString("019c1ab0-5500-7000-8000-000000000001")
        val broadcast = createBroadcast()
        val connectionId = register(registry, userId, emitter)

        registry.send(broadcast)
        registry.completeReplay(
            userId = userId,
            connectionId = connectionId,
            replayEvents = emptyList(),
            replayBroadcasts = listOf(broadcast),
        )
        registry.send(broadcast)

        verify(emitter, times(1)).send(any<SseEmitter.SseEventBuilder>())
    }

    @Test
    fun `fans live broadcast out to every connected user`() {
        val registry = registry()
        val firstEmitter = mock<SseEmitter>()
        val secondEmitter = mock<SseEmitter>()
        val firstUserId = UUID.fromString("019c1ab0-5500-7000-8000-000000000002")
        val secondUserId = UUID.fromString("019c1ab0-5500-7000-8000-000000000003")
        val firstConnectionId = register(registry, firstUserId, firstEmitter)
        val secondConnectionId = register(registry, secondUserId, secondEmitter)

        registry.completeReplay(
            userId = firstUserId,
            connectionId = firstConnectionId,
            replayEvents = emptyList(),
            replayBroadcasts = emptyList(),
        )
        registry.completeReplay(
            userId = secondUserId,
            connectionId = secondConnectionId,
            replayEvents = emptyList(),
            replayBroadcasts = emptyList(),
        )

        registry.send(createBroadcast())

        verify(firstEmitter, times(1)).send(any<SseEmitter.SseEventBuilder>())
        verify(secondEmitter, times(1)).send(any<SseEmitter.SseEventBuilder>())
    }

    private fun register(
        registry: SseConnectionRegistry,
        userId: UUID,
        emitter: SseEmitter,
    ): UUID =
        registry.register(
            userId = userId,
            clientIp = "203.0.113.11",
            emitter = emitter,
            dropSpotlightPresentationMode = DropSpotlightPresentationMode.FULL,
        )

    private fun registry(): SseConnectionRegistry =
        SseConnectionRegistry(
            properties = RealtimeDeliveryProperties(),
            telemetry = RecordingRealtimeEventSink(),
            leaseStore = FakeLeaseStore(),
        )

    private fun createBroadcast(): RealtimeBroadcast {
        val spotlightId = UUID.fromString("019c1ab0-5500-7000-8000-000000000010")
        val publishedAt = Instant.parse("2026-08-16T00:00:00Z")
        val expiresAt = Instant.parse("2026-08-16T01:00:00Z")
        val payload =
            DropSpotlightBroadcastPayload(
                spotlightId = spotlightId,
                sequence = 7,
                sourceAccountId = UUID.fromString("019c1ab0-5500-7000-8000-000000000011"),
                itemDropEventId = UUID.fromString("019c1ab0-5500-7000-8000-000000000012"),
                droppedFor = "Knalis",
                itemId = UUID.fromString("019c1ab0-5500-7000-8000-000000000013"),
                itemName = "Elucidator",
                itemRarity = "MYTHICAL",
                itemType = "SWORD",
                notificationPack = null,
                publishedAt = publishedAt,
                expiresAt = expiresAt,
                minimumPresentationGapSeconds = 30,
            )

        return RealtimeBroadcast(
            id = spotlightId,
            sequence = payload.sequence,
            occurredAt = publishedAt,
            expiresAt = expiresAt,
            payload = payload,
        )
    }

    private class FakeLeaseStore : SseConnectionLeaseStore {
        override fun acquire(
            userId: UUID,
            clientIp: String,
            connectionId: UUID,
        ): SseConnectionLeaseAcquireResult = SseConnectionLeaseAcquireResult.ACQUIRED

        override fun renew(
            userId: UUID,
            clientIp: String,
            connectionId: UUID,
        ): Boolean = true

        override fun release(
            userId: UUID,
            clientIp: String,
            connectionId: UUID,
        ) = Unit
    }
}
