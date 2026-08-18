package com.mnemos.realtime.adapter.sse.connection

import com.mnemos.realtime.application.broadcast.model.DropSpotlightPresentationMode
import com.mnemos.realtime.application.delivery.model.RealtimeEvent
import com.mnemos.realtime.application.delivery.model.RealtimeEventType
import com.mnemos.realtime.application.delivery.model.WalletTransactionCompletedPayload
import com.mnemos.realtime.configuration.delivery.RealtimeDeliveryProperties
import com.mnemos.realtime.support.RecordingRealtimeEventSink
import org.junit.jupiter.api.Assertions.assertDoesNotThrow
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doThrow
import org.mockito.kotlin.mock
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter
import java.time.Instant
import java.util.UUID

class SseConnectionRegistryTest {
    @Test
    fun `merges replay and live events without sending duplicates`() {
        val registry = registry()
        val emitter = mock<SseEmitter>()
        val userId = UUID.fromString("019c1129-ef54-7000-8000-000000000020")
        val replayedEvent = createEvent(userId, "019c1129-ef54-7000-8000-000000000021")
        val liveEvent = createEvent(userId, "019c1129-ef54-7000-8000-000000000022")
        val connectionId = register(registry, userId, emitter)

        registry.send(replayedEvent)
        registry.completeReplay(
            userId = userId,
            connectionId = connectionId,
            replayEvents = listOf(replayedEvent),
            replayBroadcasts = emptyList(),
        )
        registry.send(replayedEvent)

        verify(emitter, times(1)).send(any<SseEmitter.SseEventBuilder>())

        registry.send(liveEvent)

        verify(emitter, times(2)).send(any<SseEmitter.SseEventBuilder>())
    }

    @Test
    fun `propagates a replay delivery failure to the connection owner`() {
        val registry = registry()
        val emitter = mock<SseEmitter>()
        val userId = UUID.fromString("019c1129-ef54-7000-8000-000000000023")
        val replayedEvent = createEvent(userId, "019c1129-ef54-7000-8000-000000000024")
        val connectionId = register(registry, userId, emitter)
        val failure = IllegalStateException("send failed")

        doThrow(failure)
            .whenever(emitter)
            .send(any<SseEmitter.SseEventBuilder>())

        assertThrows(IllegalStateException::class.java) {
            registry.completeReplay(
                userId = userId,
                connectionId = connectionId,
                replayEvents = listOf(replayedEvent),
                replayBroadcasts = emptyList(),
            )
        }
    }

    @Test
    fun `rejects connections above per user limit`() {
        val registry =
            registry(
                properties =
                    RealtimeDeliveryProperties(
                        maxConnectionsPerUser = 1,
                        maxConnectionsPerClientIp = 10,
                        maxConnectionsTotal = 10,
                    ),
            )
        val userId = UUID.fromString("019c1129-ef54-7000-8000-000000000025")

        register(registry, userId)

        assertThrows(RealtimeConnectionLimitExceededException::class.java) {
            register(registry, userId)
        }
    }

    @Test
    fun `rejects connections above instance limit`() {
        val registry =
            registry(
                properties =
                    RealtimeDeliveryProperties(
                        maxConnectionsPerUser = 1,
                        maxConnectionsPerClientIp = 1,
                        maxConnectionsTotal = 1,
                    ),
            )

        register(
            registry = registry,
            userId = UUID.fromString("019c1129-ef54-7000-8000-000000000026"),
        )

        assertThrows(RealtimeConnectionLimitExceededException::class.java) {
            register(
                registry = registry,
                userId = UUID.fromString("019c1129-ef54-7000-8000-000000000027"),
            )
        }
    }

    @Test
    fun `rejects connection when cluster lease is denied`() {
        val leaseStore =
            FakeSseConnectionLeaseStore(
                acquireResult = SseConnectionLeaseAcquireResult.CLIENT_IP_LIMIT_EXCEEDED,
            )
        val registry = registry(leaseStore = leaseStore)

        assertThrows(RealtimeConnectionLimitExceededException::class.java) {
            register(
                registry = registry,
                userId = UUID.fromString("019c1129-ef54-7000-8000-000000000028"),
            )
        }

        assertTrue(leaseStore.releasedConnectionIds.isEmpty())
    }

    @Test
    fun `releases local and cluster quota when connection is unregistered`() {
        val leaseStore = FakeSseConnectionLeaseStore()
        val registry =
            registry(
                properties =
                    RealtimeDeliveryProperties(
                        maxConnectionsPerUser = 1,
                        maxConnectionsPerClientIp = 1,
                        maxConnectionsTotal = 1,
                    ),
                leaseStore = leaseStore,
            )
        val firstUserId = UUID.fromString("019c1129-ef54-7000-8000-000000000029")
        val secondUserId = UUID.fromString("019c1129-ef54-7000-8000-000000000030")
        val connectionId = register(registry, firstUserId)

        registry.unregister(
            userId = firstUserId,
            connectionId = connectionId,
        )

        assertEquals(setOf(connectionId), leaseStore.releasedConnectionIds)
        assertDoesNotThrow {
            register(registry, secondUserId)
        }
    }

    private fun register(
        registry: SseConnectionRegistry,
        userId: UUID,
        emitter: SseEmitter = mock(),
    ): UUID =
        registry.register(
            userId = userId,
            clientIp = CLIENT_IP,
            emitter = emitter,
            dropSpotlightPresentationMode = DropSpotlightPresentationMode.FULL,
        )

    private fun registry(
        properties: RealtimeDeliveryProperties = RealtimeDeliveryProperties(),
        leaseStore: SseConnectionLeaseStore = FakeSseConnectionLeaseStore(),
    ): SseConnectionRegistry =
        SseConnectionRegistry(
            properties = properties,
            telemetry = RecordingRealtimeEventSink(),
            leaseStore = leaseStore,
        )

    private fun createEvent(
        userId: UUID,
        eventId: String,
    ): RealtimeEvent =
        RealtimeEvent(
            id = UUID.fromString(eventId),
            userId = userId,
            type = RealtimeEventType.WALLET_TRANSACTION_COMPLETED,
            occurredAt = Instant.parse("2026-08-15T10:00:00Z"),
            operationId = null,
            payload =
                WalletTransactionCompletedPayload(
                    requestId = UUID.randomUUID(),
                    transactionId = UUID.randomUUID(),
                    ownerType = "ACCOUNT",
                    ownerId = userId,
                    currency = "COIN",
                    source = "BET_PLACE",
                    sourceReferenceId = UUID.randomUUID(),
                    amount = 500,
                ),
        )

    private class FakeSseConnectionLeaseStore(
        var acquireResult: SseConnectionLeaseAcquireResult = SseConnectionLeaseAcquireResult.ACQUIRED,
    ) : SseConnectionLeaseStore {
        val releasedConnectionIds = mutableSetOf<UUID>()

        override fun acquire(
            userId: UUID,
            clientIp: String,
            connectionId: UUID,
        ): SseConnectionLeaseAcquireResult = acquireResult

        override fun renew(
            userId: UUID,
            clientIp: String,
            connectionId: UUID,
        ): Boolean = true

        override fun release(
            userId: UUID,
            clientIp: String,
            connectionId: UUID,
        ) {
            releasedConnectionIds += connectionId
        }
    }

    private companion object {
        const val CLIENT_IP = "203.0.113.10"
    }
}
