package com.mnemos.telegram.bot.adapter.out.telegram

import com.mnemos.telegram.bot.application.message.model.TelegramDeliveryResult
import com.mnemos.telegram.bot.application.message.model.TelegramMessageSendResult
import com.pengrad.telegrambot.response.BaseResponse
import com.pengrad.telegrambot.response.SendResponse

object TelegramDeliveryResultMapper {
    fun BaseResponse.toDeliveryResult(): TelegramDeliveryResult =
        when {
            isOk() -> TelegramDeliveryResult.Delivered
            isMessageNotModified() -> TelegramDeliveryResult.Delivered
            errorCode() == 403 -> TelegramDeliveryResult.PermanentFailure("TELEGRAM_BLOCKED")
            errorCode() == 429 -> TelegramDeliveryResult.Retryable
            errorCode() >= 500 -> TelegramDeliveryResult.Retryable
            else ->
                TelegramDeliveryResult.PermanentFailure(
                    reason = description() ?: "Telegram delivery failed",
                )
        }

    fun SendResponse.toMessageSendResult(): TelegramMessageSendResult {
        if (isOk()) {
            val messageId = message()?.messageId() ?: return TelegramMessageSendResult.Retryable

            return TelegramMessageSendResult.Delivered(messageId)
        }

        return when (val result = toDeliveryResult()) {
            TelegramDeliveryResult.Delivered -> TelegramMessageSendResult.Retryable
            TelegramDeliveryResult.Retryable -> TelegramMessageSendResult.Retryable
            is TelegramDeliveryResult.PermanentFailure ->
                TelegramMessageSendResult.PermanentFailure(result.reason)
        }
    }

    private fun BaseResponse.isMessageNotModified(): Boolean =
        errorCode() == 400 &&
            description()
                ?.contains("message is not modified", ignoreCase = true) == true
}
