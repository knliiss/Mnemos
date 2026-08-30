package com.mnemos.telegram.bot.contracts.internal.subscription

data class TelegramCreateItemDropSubscriptionRequest(
    val droppedFor: String?,
    val itemName: String?,
)
