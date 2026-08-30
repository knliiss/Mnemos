package com.mnemos.notification.adapter.out.telegram

import com.mnemos.common.core.model.id.TelegramId
import com.mnemos.notification.application.notification.delivery.model.DeliveryResult
import com.mnemos.notification.application.notification.delivery.model.MessageAction
import com.mnemos.notification.application.notification.delivery.model.MessageParseMode
import com.mnemos.notification.application.notification.delivery.model.RenderedMessage
import com.mnemos.telegram.bot.contracts.internal.message.EditTelegramMessageRequest
import com.mnemos.telegram.bot.contracts.internal.message.SendTelegramMessageRequest
import com.mnemos.telegram.bot.contracts.internal.message.TelegramDeliveryResponse
import com.mnemos.telegram.bot.contracts.internal.message.TelegramDeliveryStatus
import com.mnemos.telegram.bot.contracts.internal.message.TelegramMessageActionPayload
import com.mnemos.telegram.bot.contracts.internal.message.TelegramMessageActionType
import com.mnemos.telegram.bot.contracts.internal.message.TelegramMessageParseMode
import com.mnemos.telegram.bot.contracts.internal.message.TelegramMessagePayload

object TelegramBotServiceMessageMapper {
    fun RenderedMessage.toSendRequest(telegramId: TelegramId): SendTelegramMessageRequest =
        SendTelegramMessageRequest(
            telegramId = telegramId.id,
            message = toPayload(),
        )

    fun RenderedMessage.toEditRequest(
        telegramId: TelegramId,
        messageId: Int,
    ): EditTelegramMessageRequest =
        EditTelegramMessageRequest(
            telegramId = telegramId.id,
            messageId = messageId,
            message = toPayload(),
        )

    fun TelegramDeliveryResponse.toDomain(): DeliveryResult =
        when (status) {
            TelegramDeliveryStatus.DELIVERED -> DeliveryResult.Delivered
            TelegramDeliveryStatus.RETRYABLE -> DeliveryResult.Retryable()
            TelegramDeliveryStatus.PERMANENT_FAILURE ->
                DeliveryResult.PermanentFailure(
                    reason = reason ?: "Telegram delivery failed",
                )
        }

    private fun RenderedMessage.toPayload(): TelegramMessagePayload =
        TelegramMessagePayload(
            text = text,
            parseMode = parseMode.toContract(),
            actions = actions.map(MessageAction::toContract),
        )

    private fun MessageParseMode.toContract(): TelegramMessageParseMode =
        when (this) {
            MessageParseMode.HTML -> TelegramMessageParseMode.HTML
            MessageParseMode.MARKDOWN_V2 -> TelegramMessageParseMode.MARKDOWN_V2
            MessageParseMode.PLAIN -> TelegramMessageParseMode.PLAIN
        }

    private fun MessageAction.toContract(): TelegramMessageActionPayload =
        when (this) {
            is MessageAction.OpenMiniApp ->
                TelegramMessageActionPayload(
                    type = TelegramMessageActionType.OPEN_MINI_APP,
                    label = label,
                )

            is MessageAction.Callback ->
                TelegramMessageActionPayload(
                    type = TelegramMessageActionType.CALLBACK,
                    label = label,
                    data = data,
                )
        }
}
