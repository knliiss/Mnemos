package com.mnemos.notification.application.notification.delivery.port.out

import com.mnemos.common.core.model.id.TelegramId
import com.mnemos.notification.application.notification.delivery.model.DeliveryResult
import com.mnemos.notification.application.notification.delivery.model.RenderedMessage

interface MessageEditGateway {
    fun edit(
        telegramId: TelegramId,
        messageId: Int,
        message: RenderedMessage,
    ): DeliveryResult
}
