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
    val label: String
    val style: OutboundTelegramButtonStyle
    val accented: Boolean

    data class OpenMiniApp(
        override val label: String,
        override val style: OutboundTelegramButtonStyle = OutboundTelegramButtonStyle.PRIMARY,
        override val accented: Boolean = true,
    ) : OutboundTelegramMessageAction

    data class OpenUrl(
        override val label: String,
        val url: String,
        override val style: OutboundTelegramButtonStyle = OutboundTelegramButtonStyle.DEFAULT,
        override val accented: Boolean = false,
    ) : OutboundTelegramMessageAction

    data class Callback(
        override val label: String,
        val data: String,
        override val style: OutboundTelegramButtonStyle = OutboundTelegramButtonStyle.DEFAULT,
        override val accented: Boolean = false,
    ) : OutboundTelegramMessageAction
}
