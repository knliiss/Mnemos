package com.mnemos.event.adapter.inbound.http.feed

import com.mnemos.event.application.feed.model.BoosterFeedItem
import com.mnemos.event.application.feed.model.EventFeedItem
import com.mnemos.event.application.feed.model.EventFeedPage
import com.mnemos.event.application.feed.model.GlobalFeedItem
import com.mnemos.event.application.feed.model.ItemDropFeedItem
import com.mnemos.event.application.feed.model.RaidFeedItem

object EventFeedApiMapper {
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
                    activatedBy = activatedBy,
                )

            is GlobalFeedItem ->
                GlobalFeedItemResponse(
                    eventId = eventId.id,
                    occurredAt = occurredAt,
                    globalType = globalType,
                )

            is RaidFeedItem ->
                RaidFeedItemResponse(
                    eventId = eventId.id,
                    occurredAt = occurredAt,
                    locations = locations,
                )
        }
}
