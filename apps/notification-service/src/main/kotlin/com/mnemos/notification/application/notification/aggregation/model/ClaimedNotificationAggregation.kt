package com.mnemos.notification.application.notification.aggregation.model

import com.mnemos.common.core.model.id.UserId
import com.mnemos.notification.application.notification.model.NotificationItem
import java.util.UUID

data class ClaimedNotificationAggregation(
    val claimId: UUID,
    val userId: UserId,
    val items: List<NotificationItem>,
) {
    init {
        require(items.isNotEmpty())
    }
}
