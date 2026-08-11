package com.mnemos.notification.adapter.inbound.http.subscription

import com.mnemos.common.core.model.id.UserId
import com.mnemos.notification.adapter.inbound.http.common.pagination.PageResponse
import com.mnemos.notification.adapter.inbound.http.common.pagination.toPageResponse
import com.mnemos.notification.application.common.pagination.PageQuery
import com.mnemos.notification.application.common.pagination.PageResult
import com.mnemos.notification.application.subscription.model.SubscriptionView
import com.mnemos.notification.application.subscription.usecase.list.ListSubscriptionsQuery

object SubscriptionApiMapper {
    fun ListSubscriptionsRequest.toQuery(userId: UserId): ListSubscriptionsQuery =
        ListSubscriptionsQuery(
            userId = userId,
            page = PageQuery(page = page, size = size),
        )

    fun PageResult<SubscriptionView>.toResponse(): PageResponse<SubscriptionResponse> = toPageResponse { it.toResponse() }

    private fun SubscriptionView.toResponse(): SubscriptionResponse =
        SubscriptionResponse(
            id = id.id,
            type = type,
            createdAt = createdAt,
            details =
                when (val details = details) {
                    is SubscriptionView.Details.ItemDrop ->
                        SubscriptionResponse.Details.ItemDrop(
                            droppedFor = details.droppedFor,
                            itemName = details.itemName,
                        )

                    is SubscriptionView.Details.Booster ->
                        SubscriptionResponse.Details.Booster(details.boosterType)

                    is SubscriptionView.Details.Dungeon ->
                        SubscriptionResponse.Details.Dungeon(details.dungeonType)

                    is SubscriptionView.Details.Raid ->
                        SubscriptionResponse.Details.Raid(details.locations)

                    is SubscriptionView.Details.Global ->
                        SubscriptionResponse.Details.Global(details.eventType)
                },
        )
}
