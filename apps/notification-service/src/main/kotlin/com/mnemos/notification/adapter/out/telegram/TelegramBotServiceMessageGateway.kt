package com.mnemos.notification.adapter.out.telegram

import com.mnemos.common.core.model.id.TelegramId
import com.mnemos.notification.adapter.out.telegram.TelegramBotServiceMessageMapper.toDomain
import com.mnemos.notification.adapter.out.telegram.TelegramBotServiceMessageMapper.toEditRequest
import com.mnemos.notification.adapter.out.telegram.TelegramBotServiceMessageMapper.toSendRequest
import com.mnemos.notification.application.notification.delivery.model.DeliveryResult
import com.mnemos.notification.application.notification.delivery.model.RenderedMessage
import com.mnemos.notification.application.notification.delivery.port.out.MessageDeliveryGateway
import com.mnemos.notification.application.notification.delivery.port.out.MessageEditGateway
import org.springframework.stereotype.Component

@Component
class TelegramBotServiceMessageGateway(
    private val client: TelegramBotServiceClient,
) : MessageDeliveryGateway,
    MessageEditGateway {
    override fun deliver(
        telegramId: TelegramId,
        message: RenderedMessage,
    ): DeliveryResult =
        try {
            client.send(message.toSendRequest(telegramId)).toDomain()
        } catch (_: RuntimeException) {
            DeliveryResult.Retryable()
        }

    override fun edit(
        telegramId: TelegramId,
        messageId: Int,
        message: RenderedMessage,
    ): DeliveryResult =
        try {
            client.edit(message.toEditRequest(telegramId, messageId)).toDomain()
        } catch (_: RuntimeException) {
            DeliveryResult.Retryable()
        }
}
