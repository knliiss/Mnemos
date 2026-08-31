package com.mnemos.telegram.bot.application.message.model

data class OutboundTelegramMessage(
    val text: String,
    val parseMode: OutboundTelegramMessageParseMode,
    val actions: List<OutboundTelegramMessageAction> = emptyList(),
    val actionRows: List<List<OutboundTelegramMessageAction>> = emptyList(),
) {
    init {
        require(text.isNotBlank())
        require(text.length <= 4096)
        require(actionRows.none(List<OutboundTelegramMessageAction>::isEmpty))
    }

    fun keyboardRows(): List<List<OutboundTelegramMessageAction>> =
        if (actionRows.isNotEmpty()) {
            actionRows
        } else {
            emptyList()
        }
}

enum class OutboundTelegramMessageParseMode {
    HTML,
    MARKDOWN_V2,
    PLAIN,
}

enum class OutboundTelegramButtonStyle(
    val telegramValue: String?,
) {
    DEFAULT(null),
    PRIMARY("primary"),
    SUCCESS("success"),
    DANGER("danger"),
}

sealed interface OutboundTelegramMessageAction {
    val style: OutboundTelegramButtonStyle

    data class OpenMiniApp(
        val label: String,
        override val style: OutboundTelegramButtonStyle = OutboundTelegramButtonStyle.PRIMARY,
    ) : OutboundTelegramMessageAction

    data class OpenUrl(
        val label: String,
        val url: String,
        override val style: OutboundTelegramButtonStyle = OutboundTelegramButtonStyle.DEFAULT,
    ) : OutboundTelegramMessageAction

    data class Callback(
        val label: String,
        val data: String,
        override val style: OutboundTelegramButtonStyle = OutboundTelegramButtonStyle.DEFAULT,
    ) : OutboundTelegramMessageAction
}
