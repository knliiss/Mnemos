package com.mnemos.telegram.bot.adapter.inbound.http.internal

import com.mnemos.telegram.bot.application.message.model.OutboundTelegramMessage
import com.mnemos.telegram.bot.application.message.model.OutboundTelegramMessageAction
import com.mnemos.telegram.bot.application.message.model.OutboundTelegramMessageParseMode
import com.mnemos.telegram.bot.application.message.model.TelegramDeliveryResult
import com.mnemos.telegram.bot.contracts.internal.message.TelegramDeliveryResponse
import com.mnemos.telegram.bot.contracts.internal.message.TelegramDeliveryStatus
import com.mnemos.telegram.bot.contracts.internal.message.TelegramMessageActionPayload
import com.mnemos.telegram.bot.contracts.internal.message.TelegramMessageActionType
import com.mnemos.telegram.bot.contracts.internal.message.TelegramMessageParseMode
import com.mnemos.telegram.bot.contracts.internal.message.TelegramMessagePayload

object TelegramMessageApiMapper {
    fun TelegramMessagePayload.toDomain(): OutboundTelegramMessage =
        OutboundTelegramMessage(
            text = text,
            parseMode = parseMode.toDomain(),
            actions = actions.map { action -> action.toDomain() },
        )

    fun TelegramDeliveryResult.toResponse(): TelegramDeliveryResponse =
        when (this) {
            TelegramDeliveryResult.Delivered ->
                TelegramDeliveryResponse(
                    status = TelegramDeliveryStatus.DELIVERED,
                )

            TelegramDeliveryResult.Retryable ->
                TelegramDeliveryResponse(
                    status = TelegramDeliveryStatus.RETRYABLE,
                )

            is TelegramDeliveryResult.PermanentFailure ->
                TelegramDeliveryResponse(
                    status = TelegramDeliveryStatus.PERMANENT_FAILURE,
                    reason = reason,
                )
        }

    private fun TelegramMessageParseMode.toDomain(): OutboundTelegramMessageParseMode =
        when (this) {
            TelegramMessageParseMode.HTML -> OutboundTelegramMessageParseMode.HTML
            TelegramMessageParseMode.MARKDOWN_V2 -> OutboundTelegramMessageParseMode.MARKDOWN_V2
            TelegramMessageParseMode.PLAIN -> OutboundTelegramMessageParseMode.PLAIN
        }

    private fun TelegramMessageActionPayload.toDomain(): OutboundTelegramMessageAction =
        when (type) {
            TelegramMessageActionType.OPEN_MINI_APP ->
                OutboundTelegramMessageAction.OpenMiniApp(
                    label = label,
                )

            TelegramMessageActionType.CALLBACK ->
                OutboundTelegramMessageAction.Callback(
                    label = label,
                    data = requireNotNull(data) { "Callback action data is required" },
                )
        }
}
