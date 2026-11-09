package com.mnemos.telegram.bot.application.widget.model

import java.nio.charset.StandardCharsets

data class TelegramWidgetButton(
    val label: String,
    val callbackData: String,
) {
    init {
        require(label.isNotBlank())
        require(callbackData.toByteArray(StandardCharsets.UTF_8).size in 1..64)
    }
}
