package com.mnemos.telegram.bot.contracts.internal.bet

import java.util.UUID

data class TelegramBetOperationResponse(
    val resourceId: UUID,
    val operationId: UUID,
)
