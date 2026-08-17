package com.mnemos.realtime.adapter.inbound.messaging.social

import com.mnemos.personalization.contracts.event.NotificationPackSnapshotV1
import com.mnemos.realtime.application.broadcast.model.DropSpotlightBroadcastPayload
import com.mnemos.realtime.application.broadcast.model.NotificationPackBroadcastSnapshot
import com.mnemos.realtime.application.broadcast.model.NotificationPackBroadcastTemplate
import com.mnemos.realtime.application.broadcast.model.RealtimeBroadcast
import com.mnemos.social.contracts.event.DropSpotlightPublishedV1

object DropSpotlightPublishedMessageMapper {
    fun DropSpotlightPublishedV1.toRealtimeBroadcast(): RealtimeBroadcast =
        RealtimeBroadcast(
            id = spotlightId,
            sequence = sequence,
            occurredAt = publishedAt,
            expiresAt = expiresAt,
            payload =
                DropSpotlightBroadcastPayload(
                    spotlightId = spotlightId,
                    sequence = sequence,
                    sourceAccountId = sourceAccountId,
                    itemDropEventId = itemDropEventId,
                    droppedFor = droppedFor,
                    itemId = itemId,
                    itemName = itemName,
                    itemRarity = itemRarity,
                    itemType = itemType,
                    notificationPack = notificationPack?.toBroadcastSnapshot(),
                    publishedAt = publishedAt,
                    expiresAt = expiresAt,
                    minimumPresentationGapSeconds = minimumPresentationGapSeconds,
                ),
        )

    private fun NotificationPackSnapshotV1.toBroadcastSnapshot(): NotificationPackBroadcastSnapshot =
        NotificationPackBroadcastSnapshot(
            mixedAggregateTitle = mixedAggregateTitle,
            templates =
                templates.map { template ->
                    NotificationPackBroadcastTemplate(
                        type = template.type.name,
                        emoji = template.emoji,
                        soloTitle = template.soloTitle,
                        soloBody = template.soloBody,
                        compact = template.compact,
                        aggregateTitle = template.aggregateTitle,
                    )
                },
        )
}
