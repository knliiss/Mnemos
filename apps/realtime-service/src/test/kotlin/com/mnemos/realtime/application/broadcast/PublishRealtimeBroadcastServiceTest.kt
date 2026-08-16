package com.mnemos.realtime.application.broadcast

import com.mnemos.realtime.application.broadcast.model.RealtimeBroadcast
import com.mnemos.realtime.application.broadcast.port.out.RealtimeBroadcastFanout
import com.mnemos.realtime.application.broadcast.port.out.RealtimeBroadcastReplayStore
import com.mnemos.social.contracts.event.DropSpotlightPublishedV1
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset
import java.util.UUID

class PublishRealtimeBroadcastServiceTest {
    @Test
    fun `publishes active broadcast to replay and fanout`() {
        val now = Instant.parse("2026-08-16T00:00:00Z")
        val replayStore = mock<RealtimeBroadcastReplayStore>()
        val fanout = mock<RealtimeBroadcastFanout>()
        val service =
            PublishRealtimeBroadcastService(
                replayStore = replayStore,
                fanout = fanout,
                clock = Clock.fixed(now, ZoneOffset.UTC),
            )
        val broadcast = createBroadcast(expiresAt = now.plusSeconds(3600))

        service.execute(broadcast)

        verify(replayStore).append(broadcast)
        verify(fanout).publish(broadcast)
    }

    @Test
    fun `ignores broadcast that is already expired`() {
        val now = Instant.parse("2026-08-16T01:00:00Z")
        val replayStore = mock<RealtimeBroadcastReplayStore>()
        val fanout = mock<RealtimeBroadcastFanout>()
        val service =
            PublishRealtimeBroadcastService(
                replayStore = replayStore,
                fanout = fanout,
                clock = Clock.fixed(now, ZoneOffset.UTC),
            )
        val broadcast = createBroadcast(expiresAt = now)

        service.execute(broadcast)

        verify(replayStore, never()).append(broadcast)
        verify(fanout, never()).publish(broadcast)
    }

    private fun createBroadcast(expiresAt: Instant): RealtimeBroadcast {
        val spotlightId = UUID.fromString("019c1ab0-4400-7000-8000-000000000001")
        val publishedAt = Instant.parse("2026-08-16T00:00:00Z")
        val event =
            DropSpotlightPublishedV1(
                spotlightId = spotlightId,
                sequence = 10,
                sourceAccountId = UUID.fromString("019c1ab0-4400-7000-8000-000000000002"),
                itemDropEventId = UUID.fromString("019c1ab0-4400-7000-8000-000000000003"),
                droppedFor = "Knalis",
                itemId = UUID.fromString("019c1ab0-4400-7000-8000-000000000004"),
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
            sequence = event.sequence,
            occurredAt = publishedAt,
            expiresAt = expiresAt,
            payload = event,
        )
    }
}
