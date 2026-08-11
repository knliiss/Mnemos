package com.mnemos.notification.application.notification.delivery.model

import com.mnemos.common.core.model.id.UserId
import com.mnemos.notification.application.notification.model.NotificationItem
import com.mnemos.notification.application.recipient.model.RecipientView
import java.time.Instant

data class DeliveryCandidate(
    val id: DeliveryCandidateId,
    val recipient: RecipientView,
    val items: List<NotificationItem>,
    val priority: DeliveryPriority,
    val readyAt: Instant,
) {
    init {
        require(items.isNotEmpty())
    }

    val userId: UserId
        get() = recipient.userId
}
