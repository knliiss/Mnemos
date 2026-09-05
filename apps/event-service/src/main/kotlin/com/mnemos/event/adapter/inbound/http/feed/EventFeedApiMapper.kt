package com.mnemos.event.adapter.inbound.http.feed

import com.mnemos.event.application.feed.model.BoosterFeedItem
import com.mnemos.event.application.feed.model.EventFeedItem
import com.mnemos.event.application.feed.model.EventFeedKind
import com.mnemos.event.application.feed.model.EventFeedPage
import com.mnemos.event.application.feed.model.EventFeedQuery
import com.mnemos.event.application.feed.model.GlobalFeedItem
import com.mnemos.event.application.feed.model.ItemDropFeedItem
import com.mnemos.event.application.feed.model.RaidFeedItem
import com.mnemos.event.application.localization.model.SaoEventLocalizationKeys
import com.mnemos.event.domain.EventId
import com.mnemos.event.domain.wipe.model.WipeId

object EventFeedApiMapper {
    fun ListEventFeedRequest.toQuery(): EventFeedQuery =
        EventFeedQuery(
            after = after?.let(::EventId),
            limit = limit,
            kinds = type.takeIf { values -> values.isNotEmpty() } ?: EventFeedKind.entries.toSet(),
            search = search,
            droppedFor = droppedFor,
            itemRarity = itemRarity,
            itemType = itemType,
            boosterType = boosterType,
            globalType = globalType,
            raidLocation = raidLocation,
            sort = sort,
            wipeId = wipeId?.let(::WipeId),
        )

    fun EventFeedPage.toResponse(): EventFeedResponse =
        EventFeedResponse(
            items = items.map { item -> item.toResponse() },
            cursor = cursor?.id,
        )

    fun EventFeedItem.toResponse(): EventFeedItemResponse =
        when (this) {
            is ItemDropFeedItem ->
                ItemDropFeedItemResponse(
                    eventId = eventId.id,
                    occurredAt = occurredAt,
                    itemId = itemId.id,
                    itemKey = itemKey,
                    itemName = itemName,
                    itemRarity = itemRarity,
                    itemType = itemType,
                    droppedFor = droppedFor,
                )

            is BoosterFeedItem ->
                BoosterFeedItemResponse(
                    eventId = eventId.id,
                    occurredAt = occurredAt,
                    boosterType = boosterType,
                    localizationKey = SaoEventLocalizationKeys.booster(boosterType),
                    activatedBy = activatedBy,
                )

            is GlobalFeedItem ->
                GlobalFeedItemResponse(
                    eventId = eventId.id,
                    occurredAt = occurredAt,
                    globalType = globalType,
                    localizationKey = SaoEventLocalizationKeys.global(globalType),
                )

            is RaidFeedItem ->
                RaidFeedItemResponse(
                    eventId = eventId.id,
                    occurredAt = occurredAt,
                    locations = locations,
                    locationLocalizationKeys = locations.map(SaoEventLocalizationKeys::location),
                )
        }
}
