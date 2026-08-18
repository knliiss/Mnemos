package com.mnemos.realtime.adapter.inbound.messaging.event

import com.mnemos.event.contracts.EventEventType
import com.mnemos.event.contracts.contract.v1.BoosterActivatedV1
import com.mnemos.event.contracts.contract.v1.GlobalStartedV1
import com.mnemos.event.contracts.contract.v1.ItemDroppedV1
import com.mnemos.event.contracts.contract.v1.RaidStartedV1
import com.mnemos.realtime.application.broadcast.model.GameEventBroadcastPayload
import com.mnemos.realtime.application.broadcast.model.RealtimeBroadcast
import com.mnemos.realtime.application.broadcast.model.RealtimeBroadcastType
import java.time.Duration
import java.time.Instant
import java.util.UUID

object GameEventBroadcastMapper {
    fun ItemDroppedV1.toBroadcast(
        eventId: UUID,
        occurredAt: Instant,
    ): RealtimeBroadcast =
        gameEventBroadcast(
            eventId = eventId,
            occurredAt = occurredAt,
            payload =
                GameEventBroadcastPayload(
                    eventId = eventId,
                    eventType = EventEventType.ITEM_DROPPED,
                    occurredAt = occurredAt,
                    itemDrop =
                        GameEventBroadcastPayload.ItemDrop(
                            itemId = itemId,
                            itemName = itemName,
                            itemType = itemType.name,
                            itemRarity = itemRarity.name,
                            droppedFor = droppedFor,
                        ),
                    raid = null,
                    booster = null,
                    global = null,
                ),
        )

    fun RaidStartedV1.toBroadcast(
        eventId: UUID,
        occurredAt: Instant,
    ): RealtimeBroadcast =
        gameEventBroadcast(
            eventId = eventId,
            occurredAt = occurredAt,
            payload =
                GameEventBroadcastPayload(
                    eventId = eventId,
                    eventType = EventEventType.RAID_STARTED,
                    occurredAt = occurredAt,
                    itemDrop = null,
                    raid = GameEventBroadcastPayload.Raid(locations = locations),
                    booster = null,
                    global = null,
                ),
        )

    fun BoosterActivatedV1.toBroadcast(
        eventId: UUID,
        occurredAt: Instant,
    ): RealtimeBroadcast =
        gameEventBroadcast(
            eventId = eventId,
            occurredAt = occurredAt,
            payload =
                GameEventBroadcastPayload(
                    eventId = eventId,
                    eventType = EventEventType.BOOSTER_ACTIVATED,
                    occurredAt = occurredAt,
                    itemDrop = null,
                    raid = null,
                    booster =
                        GameEventBroadcastPayload.Booster(
                            activatedBy = activatedBy,
                            type = type.name,
                        ),
                    global = null,
                ),
        )

    fun GlobalStartedV1.toBroadcast(
        eventId: UUID,
        occurredAt: Instant,
    ): RealtimeBroadcast =
        gameEventBroadcast(
            eventId = eventId,
            occurredAt = occurredAt,
            payload =
                GameEventBroadcastPayload(
                    eventId = eventId,
                    eventType = EventEventType.GLOBAL_STARTED,
                    occurredAt = occurredAt,
                    itemDrop = null,
                    raid = null,
                    booster = null,
                    global = GameEventBroadcastPayload.Global(type = type.name),
                ),
        )

    private fun gameEventBroadcast(
        eventId: UUID,
        occurredAt: Instant,
        payload: GameEventBroadcastPayload,
    ): RealtimeBroadcast =
        RealtimeBroadcast(
            id = eventId,
            sequence = 0,
            occurredAt = occurredAt,
            expiresAt = occurredAt.plus(GAME_EVENT_LIFETIME),
            payload = null,
            type = RealtimeBroadcastType.GAME_EVENT,
            gameEventPayload = payload,
        )

    private val GAME_EVENT_LIFETIME = Duration.ofMinutes(5)
}
