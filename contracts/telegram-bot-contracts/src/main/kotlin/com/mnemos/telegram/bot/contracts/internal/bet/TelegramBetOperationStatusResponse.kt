package com.mnemos.telegram.bot.contracts.internal.bet

import java.util.UUID

enum class TelegramBetOperationStatus {
    PENDING,
    COMPLETED,
    FAILED,
}

data class TelegramBetOperationStatusResponse(
    val operationId: UUID,
    val status: TelegramBetOperationStatus,
)
