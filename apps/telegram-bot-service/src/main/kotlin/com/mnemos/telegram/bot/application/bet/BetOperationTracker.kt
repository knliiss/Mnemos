package com.mnemos.telegram.bot.application.bet

import com.mnemos.telegram.bot.application.bet.model.TelegramBetOperationState
import com.mnemos.telegram.bot.application.bet.port.out.BetGateway
import org.springframework.stereotype.Component
import java.time.Duration
import java.util.UUID
import java.util.concurrent.locks.LockSupport

@Component
class BetOperationTracker(
    private val gateway: BetGateway,
) {
    fun awaitTerminalState(
        telegramId: Long,
        operationId: UUID,
    ): TelegramBetOperationState {
        repeat(MAX_ATTEMPTS) { attempt ->
            val state = gateway.getOperationState(telegramId, operationId)

            if (state != TelegramBetOperationState.PENDING) {
                return state
            }

            if (attempt < MAX_ATTEMPTS - 1) {
                LockSupport.parkNanos(POLL_INTERVAL.toNanos())
            }
        }

        return TelegramBetOperationState.PENDING
    }

    private companion object {
        const val MAX_ATTEMPTS = 12
        val POLL_INTERVAL: Duration = Duration.ofMillis(250)
    }
}
