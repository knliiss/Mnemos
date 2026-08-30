package com.mnemos.telegram.bot.adapter.out.telegram

import com.mnemos.telegram.bot.application.message.model.TelegramDeliveryResult
import com.pengrad.telegrambot.response.BaseResponse

object TelegramDeliveryResultMapper {
    fun BaseResponse.toDeliveryResult(): TelegramDeliveryResult =
        when {
            isOk() -> TelegramDeliveryResult.Delivered
            errorCode() == 403 -> TelegramDeliveryResult.PermanentFailure("TELEGRAM_BLOCKED")
            errorCode() == 429 -> TelegramDeliveryResult.Retryable
            errorCode() >= 500 -> TelegramDeliveryResult.Retryable
            else ->
                TelegramDeliveryResult.PermanentFailure(
                    reason = description() ?: "Telegram delivery failed",
                )
        }
}
