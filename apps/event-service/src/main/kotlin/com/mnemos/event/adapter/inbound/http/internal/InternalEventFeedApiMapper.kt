package com.mnemos.event.adapter.inbound.http.internal

import com.mnemos.event.application.feed.model.BoosterFeedItem
import com.mnemos.event.application.feed.model.EventFeedItem
import com.mnemos.event.application.feed.model.EventFeedKind
import com.mnemos.event.application.feed.model.EventFeedPage
import com.mnemos.event.application.feed.model.EventFeedQuery
import com.mnemos.event.application.feed.model.EventFeedSort
import com.mnemos.event.application.feed.model.GlobalFeedItem
import com.mnemos.event.application.feed.model.ItemDropFeedItem
import com.mnemos.event.application.feed.model.RaidFeedItem
import com.mnemos.event.contracts.internal.v1.InternalEventFeedItemResponse
import com.mnemos.event.contracts.internal.v1.InternalEventFeedResponse
import com.mnemos.event.contracts.internal.v1.InternalEventFeedSort
import com.mnemos.event.contracts.internal.v1.InternalEventFeedType
import com.mnemos.event.contracts.shared.ContractBoosterType
import com.mnemos.event.contracts.shared.ContractGlobalEventType
import com.mnemos.event.contracts.shared.ContractItemRarity
import com.mnemos.event.contracts.shared.ContractItemType
import com.mnemos.event.domain.EventId
import com.mnemos.event.domain.booster.model.DomainBoosterType
import com.mnemos.event.domain.global.model.DomainGlobalEventType
import com.mnemos.event.domain.item.model.DomainItemRarity
import com.mnemos.event.domain.item.model.DomainItemType
import com.mnemos.event.domain.wipe.model.WipeId
import java.util.UUID

object InternalEventFeedApiMapper {
    fun toQuery(
        after: UUID?,
        type: InternalEventFeedType?,
        search: String?,
        droppedFor: String? = null,
        itemRarity: ContractItemRarity?,
        itemType: ContractItemType?,
        boosterType: ContractBoosterType?,
        globalType: ContractGlobalEventType?,
        raidLocation: Int?,
        wipeId: UUID?,
        sort: InternalEventFeedSort,
        limit: Int,
    ): EventFeedQuery =
        EventFeedQuery(
            after = after?.let(::EventId),
            limit = limit,
            kinds = type?.let { value -> setOf(EventFeedKind.valueOf(value.name)) } ?: EventFeedKind.entries.toSet(),
            search = search?.trim()?.takeIf(String::isNotEmpty),
            droppedFor = droppedFor?.trim()?.takeIf(String::isNotEmpty),
            itemRarity = itemRarity?.let { value -> DomainItemRarity.valueOf(value.name) },
            itemType = itemType?.let { value -> DomainItemType.valueOf(value.name) },
            boosterType = boosterType?.let { value -> DomainBoosterType.valueOf(value.name) },
            globalType = globalType?.let { value -> DomainGlobalEventType.valueOf(value.name) },
            raidLocation = raidLocation,
            sort = EventFeedSort.valueOf(sort.name),
            wipeId = wipeId?.let(::WipeId),
        )

    fun EventFeedPage.toInternalResponse(): InternalEventFeedResponse =
        InternalEventFeedResponse(
            items = items.map { item -> item.toInternalResponse() },
            cursor = cursor?.id,
        )

    fun EventFeedItem.toInternalResponse(): InternalEventFeedItemResponse =
        when (this) {
            is ItemDropFeedItem ->
                InternalEventFeedItemResponse(
                    type = InternalEventFeedType.ITEM_DROP,
                    eventId = eventId.id,
                    occurredAt = occurredAt,
                    itemId = itemId.id,
                    itemKey = itemKey,
                    itemName = itemName,
                    itemRarity = ContractItemRarity.valueOf(itemRarity.name),
                    itemType = itemType,
                    droppedFor = droppedFor,
                )

            is BoosterFeedItem ->
                InternalEventFeedItemResponse(
                    type = InternalEventFeedType.BOOSTER,
                    eventId = eventId.id,
                    occurredAt = occurredAt,
                    boosterType = ContractBoosterType.valueOf(boosterType.name),
                    activatedBy = activatedBy,
                )

            is GlobalFeedItem ->
                InternalEventFeedItemResponse(
                    type = InternalEventFeedType.GLOBAL,
                    eventId = eventId.id,
                    occurredAt = occurredAt,
                    globalType = ContractGlobalEventType.valueOf(globalType.name),
                )

            is RaidFeedItem ->
                InternalEventFeedItemResponse(
                    type = InternalEventFeedType.RAID,
                    eventId = eventId.id,
                    occurredAt = occurredAt,
                    locations = locations,
                )
        }
}
