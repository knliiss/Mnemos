package com.mnemos.telegram.bot.application.notification.port.out

interface NotificationRecipientGateway {
    fun activate(telegramId: Long)
}
