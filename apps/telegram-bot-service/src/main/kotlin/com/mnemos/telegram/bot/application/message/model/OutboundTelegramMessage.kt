package com.mnemos.telegram.bot.application.message.model

data class OutboundTelegramMessage(
    val text: String,
    val parseMode: OutboundTelegramMessageParseMode,
    val actions: List<OutboundTelegramMessageAction>,
) {
    init {
        require(text.isNotBlank())
        require(text.length <= 4096)
    }
}

enum class OutboundTelegramMessageParseMode {
    HTML,
    MARKDOWN_V2,
    PLAIN,
}

sealed interface OutboundTelegramMessageAction {
    data class OpenMiniApp(
        val label: String,
    ) : OutboundTelegramMessageAction

    data class Callback(
        val label: String,
        val data: String,
    ) : OutboundTelegramMessageAction
}
