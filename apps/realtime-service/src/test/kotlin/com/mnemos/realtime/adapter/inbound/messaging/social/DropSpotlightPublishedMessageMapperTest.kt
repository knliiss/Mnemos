package com.mnemos.realtime.adapter.inbound.messaging.social

import com.mnemos.personalization.contracts.event.NotificationPackSnapshotV1
import com.mnemos.personalization.contracts.event.NotificationPackTemplateV1
import com.mnemos.personalization.contracts.shared.NotificationPackTemplateType
import com.mnemos.realtime.adapter.inbound.messaging.social.DropSpotlightPublishedMessageMapper.toRealtimeBroadcast
import com.mnemos.social.contracts.event.DropSpotlightPublishedV1
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.time.Instant
import java.util.UUID

class DropSpotlightPublishedMessageMapperTest {
    @Test
    fun `maps social contract to internal realtime broadcast without losing presentation data`() {
        val publishedAt = Instant.parse("2026-08-16T00:00:00Z")
        val expiresAt = Instant.parse("2026-08-16T01:00:00Z")
        val pack =
            NotificationPackSnapshotV1(
                mixedAggregateTitle = "Drops",
                templates = NotificationPackTemplateType.entries.map { type -> template(type) },
            )
        val event =
            DropSpotlightPublishedV1(
                spotlightId = UUID.fromString("019c1ab0-6600-7000-8000-000000000001"),
                sequence = 17,
                sourceAccountId = UUID.fromString("019c1ab0-6600-7000-8000-000000000002"),
                itemDropEventId = UUID.fromString("019c1ab0-6600-7000-8000-000000000003"),
                droppedFor = "Knalis",
                itemId = UUID.fromString("019c1ab0-6600-7000-8000-000000000004"),
                itemName = "Elucidator",
                itemRarity = "MYTHICAL",
                itemType = "SWORD",
                notificationPack = pack,
                publishedAt = publishedAt,
                expiresAt = expiresAt,
                minimumPresentationGapSeconds = 30,
            )

        val broadcast = event.toRealtimeBroadcast()

        assertEquals(event.spotlightId, broadcast.id)
        assertEquals(event.sequence, broadcast.sequence)
        assertEquals(event.itemName, broadcast.payload.itemName)
        assertEquals(event.minimumPresentationGapSeconds, broadcast.payload.minimumPresentationGapSeconds)
        assertEquals(pack.mixedAggregateTitle, broadcast.payload.notificationPack?.mixedAggregateTitle)
        assertEquals(
            NotificationPackTemplateType.entries.map { it.name },
            broadcast.payload.notificationPack
                ?.templates
                ?.map { it.type },
        )
    }

    private fun template(type: NotificationPackTemplateType): NotificationPackTemplateV1 =
        NotificationPackTemplateV1(
            type = type,
            emoji = "⚔️",
            soloTitle = "Solo title",
            soloBody = "Solo body",
            compact = "Compact",
            aggregateTitle = "Aggregate",
        )
}
