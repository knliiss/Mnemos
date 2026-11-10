package com.mnemos.telegram.bot.application.widget.wallet.port.out

import com.mnemos.telegram.bot.application.message.model.TelegramDeliveryResult
import com.mnemos.telegram.bot.application.widget.wallet.WalletWidgetView

interface WalletWidgetPublisher {
    fun publish(
        telegramId: Long,
        view: WalletWidgetView,
    ): TelegramDeliveryResult

    fun update(
        telegramId: Long,
        messageId: Int,
        view: WalletWidgetView,
    ): TelegramDeliveryResult

    fun state(view: WalletWidgetView): String
}
