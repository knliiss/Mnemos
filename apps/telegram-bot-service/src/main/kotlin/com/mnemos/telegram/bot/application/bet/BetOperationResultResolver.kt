package com.mnemos.telegram.bot.application.bet

import com.mnemos.telegram.bot.application.bet.model.BetMenuMode
import org.springframework.stereotype.Component
import java.util.UUID

@Component
class BetOperationResultResolver(
    private val tracker: BetOperationTracker,
) {
    fun resolve(
        telegramId: Long,
        mode: BetMenuMode,
        operationId: UUID,
    ): Map<String, String> =
        BetMenuContext.operationResult(
            mode = mode,
            operationId = operationId,
            state = tracker.awaitTerminalState(telegramId, operationId),
        )
}
