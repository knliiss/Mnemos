package com.mnemos.notification.application.notification.delivery.port.out

import com.mnemos.common.core.model.id.UserId
import com.mnemos.notification.application.notification.delivery.model.RenderedMessage
import com.mnemos.notification.application.notification.model.NotificationItem
import com.mnemos.notification.application.telegram.start.TelegramStartContext

interface NotificationMessageRenderer {
    fun renderNotification(
        userId: UserId,
        items: List<NotificationItem>,
    ): RenderedMessage

    fun renderStart(context: TelegramStartContext): RenderedMessage
}
