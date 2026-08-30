package com.mnemos.notification.adapter.inbound.http.internal

import com.mnemos.notification.application.common.pagination.PageResult
import com.mnemos.notification.application.subscription.model.SubscriptionView
import com.mnemos.notification.domain.subscription.model.SubscriptionType
import com.mnemos.telegram.bot.contracts.internal.subscription.TelegramSubscriptionListResponse
import com.mnemos.telegram.bot.contracts.internal.subscription.TelegramSubscriptionResponse
import com.mnemos.telegram.bot.contracts.internal.subscription.TelegramSubscriptionType

object TelegramSubscriptionInternalApiMapper {
    fun PageResult<SubscriptionView>.toResponse(): TelegramSubscriptionListResponse =
        TelegramSubscriptionListResponse(
            items = items.map(SubscriptionView::toResponse),
            page = page,
            totalPages = totalPages,
            totalItems = totalItems,
            hasNextPage = hasNextPage,
        )

    private fun SubscriptionView.toResponse(): TelegramSubscriptionResponse =
        when (val details = details) {
            is SubscriptionView.Details.ItemDrop ->
                baseResponse(
                    droppedFor = details.droppedFor,
                    itemName = details.itemName,
                )

            is SubscriptionView.Details.Booster ->
                baseResponse(boosterType = details.boosterType.name)

            is SubscriptionView.Details.Dungeon ->
                baseResponse(dungeonType = details.dungeonType.name)

            is SubscriptionView.Details.Raid ->
                baseResponse(raidLocations = details.locations)

            is SubscriptionView.Details.Global ->
                baseResponse(globalType = details.eventType.name)
        }

    private fun SubscriptionView.baseResponse(
        droppedFor: String? = null,
        itemName: String? = null,
        boosterType: String? = null,
        dungeonType: String? = null,
        globalType: String? = null,
        raidLocations: Set<Int> = emptySet(),
    ): TelegramSubscriptionResponse =
        TelegramSubscriptionResponse(
            id = id.value,
            type = type.toContract(),
            createdAt = createdAt,
            droppedFor = droppedFor,
            itemName = itemName,
            boosterType = boosterType,
            dungeonType = dungeonType,
            globalType = globalType,
            raidLocations = raidLocations,
        )

    private fun SubscriptionType.toContract(): TelegramSubscriptionType = TelegramSubscriptionType.valueOf(name)
}
