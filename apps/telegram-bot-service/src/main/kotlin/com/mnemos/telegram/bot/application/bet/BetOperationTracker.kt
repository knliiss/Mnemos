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
    ): TelegramBetOperationState =
        awaitTerminalState(
            telegramId = telegramId,
            operationId = operationId,
            maxAttempts = MAX_ATTEMPTS,
            pollInterval = POLL_INTERVAL,
        )

    internal fun awaitTerminalState(
        telegramId: Long,
        operationId: UUID,
        maxAttempts: Int,
        pollInterval: Duration,
    ): TelegramBetOperationState {
        require(maxAttempts > 0)
        require(!pollInterval.isNegative)

        repeat(maxAttempts) { attempt ->
            val state = gateway.getOperationState(telegramId, operationId)

            if (state != TelegramBetOperationState.PENDING) {
                return state
            }

            if (attempt < maxAttempts - 1 && !pollInterval.isZero) {
                LockSupport.parkNanos(pollInterval.toNanos())
            }
        }

        return TelegramBetOperationState.PENDING
    }

    private companion object {
        const val MAX_ATTEMPTS = 12
        val POLL_INTERVAL: Duration = Duration.ofMillis(250)
    }
}
