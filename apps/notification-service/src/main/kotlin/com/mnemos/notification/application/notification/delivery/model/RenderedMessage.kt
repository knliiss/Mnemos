package com.mnemos.notification.application.notification.delivery.model

data class RenderedMessage(
    val text: String,
    val parseMode: MessageParseMode = MessageParseMode.HTML,
    val actions: List<MessageAction> = emptyList(),
) {
    init {
        require(text.isNotBlank())
        require(text.length <= 4096)
    }
}

enum class MessageParseMode {
    HTML,
    MARKDOWN_V2,
    PLAIN,
}

sealed interface MessageAction {
    data class OpenMiniApp(
        val label: String,
    ) : MessageAction {
        init {
            require(label.isNotBlank())
        }
    }
}
