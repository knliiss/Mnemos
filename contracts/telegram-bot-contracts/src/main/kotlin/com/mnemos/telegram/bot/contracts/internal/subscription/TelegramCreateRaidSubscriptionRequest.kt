package com.mnemos.telegram.bot.contracts.internal.subscription

data class TelegramCreateRaidSubscriptionRequest(
    val locations: Set<Int>,
)
