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
                            itemKey = itemKey,
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
                    raid =
                        GameEventBroadcastPayload.Raid(
                            locations = locations,
                            locationLocalizationKeys =
                                locations
                                    .sorted()
                                    .map { location -> "sao.location.$location.name" },
                        ),
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
                            localizationKey = boosterLocalizationKey(type.name),
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
                    global =
                        GameEventBroadcastPayload.Global(
                            type = type.name,
                            localizationKey = globalLocalizationKey(type.name),
                        ),
                ),
        )

    private fun boosterLocalizationKey(type: String): String =
        when (type) {
            "LUCK" -> "sao.enum.boost.luck.name"
            "MONEY" -> "sao.enum.boost.coin.name"
            "DAMAGE" -> "sao.enum.boost.damage.name"
            "POWER" -> "sao.enum.boost.power.name"
            else -> error("Unsupported booster type: $type")
        }

    private fun globalLocalizationKey(type: String): String =
        when (type) {
            "DARKNESS" -> "sao.event.darkness.alert"
            "MOON" -> "sao.event.blood_moon.alert"
            "ECLIPSE" -> "sao.event.solar_eclipse.alert"
            "EXPLOSION" -> "sao.event.sunburst.alert"
            "COMET_CHAOS" -> "sao.event.comet_chaos.alert"
            else -> error("Unsupported global event type: $type")
        }

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
