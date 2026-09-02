package com.mnemos.telegram.bot.contracts.internal.message

data class SendTelegramMessageRequest(
    val telegramId: Long,
    val message: TelegramMessagePayload,
)

data class EditTelegramMessageRequest(
    val telegramId: Long,
    val messageId: Int,
    val message: TelegramMessagePayload,
)

data class TelegramMessagePayload(
    val text: String,
    val parseMode: TelegramMessageParseMode = TelegramMessageParseMode.HTML,
    val actions: List<TelegramMessageActionPayload> = emptyList(),
)

data class TelegramMessageActionPayload(
    val type: TelegramMessageActionType,
    val label: String,
    val data: String? = null,
)

enum class TelegramMessageActionType {
    OPEN_MINI_APP,
    CALLBACK,
}

enum class TelegramMessageParseMode {
    HTML,
    MARKDOWN_V2,
    PLAIN,
}

data class TelegramDeliveryResponse(
    val status: TelegramDeliveryStatus,
    val reason: String? = null,
)

enum class TelegramDeliveryStatus {
    DELIVERED,
    RETRYABLE,
    PERMANENT_FAILURE,
}
