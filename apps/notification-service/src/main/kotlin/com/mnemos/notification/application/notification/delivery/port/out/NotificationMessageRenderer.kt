package com.mnemos.notification.application.notification.delivery.port.out

import com.mnemos.common.core.model.id.UserId
import com.mnemos.notification.application.notification.delivery.model.RenderedMessage
import com.mnemos.notification.application.notification.model.NotificationItem

interface NotificationMessageRenderer {
    fun renderNotification(
        userId: UserId,
        items: List<NotificationItem>,
    ): RenderedMessage
}
