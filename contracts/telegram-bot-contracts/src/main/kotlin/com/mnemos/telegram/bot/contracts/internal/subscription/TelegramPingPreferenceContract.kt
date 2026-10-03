package com.mnemos.telegram.bot.contracts.internal.subscription

data class TelegramPingPreferenceResponse(
    val enabled: Boolean,
)

data class TelegramUpdatePingPreferenceRequest(
    val enabled: Boolean,
)
