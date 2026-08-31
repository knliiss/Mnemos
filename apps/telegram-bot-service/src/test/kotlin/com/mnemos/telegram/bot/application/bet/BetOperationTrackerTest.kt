package com.mnemos.telegram.bot.application.bet

import com.mnemos.telegram.bot.application.bet.model.TelegramBetOperationState
import com.mnemos.telegram.bot.application.bet.port.out.BetGateway
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.time.Duration
import java.util.UUID

class BetOperationTrackerTest {
    private val gateway = mock<BetGateway>()
    private val tracker = BetOperationTracker(gateway)

    @Test
    fun `polling returns terminal operation state`() {
        val operationId = UUID.randomUUID()
        whenever(gateway.getOperationState(TELEGRAM_ID, operationId))
            .thenReturn(TelegramBetOperationState.PENDING)
            .thenReturn(TelegramBetOperationState.COMPLETED)

        val result =
            tracker.awaitTerminalState(
                telegramId = TELEGRAM_ID,
                operationId = operationId,
                maxAttempts = 3,
                pollInterval = Duration.ZERO,
            )

        assertEquals(TelegramBetOperationState.COMPLETED, result)
        verify(gateway, times(2)).getOperationState(TELEGRAM_ID, operationId)
    }

    @Test
    fun `polling keeps pending state after attempt limit`() {
        val operationId = UUID.randomUUID()
        whenever(gateway.getOperationState(TELEGRAM_ID, operationId))
            .thenReturn(TelegramBetOperationState.PENDING)

        val result =
            tracker.awaitTerminalState(
                telegramId = TELEGRAM_ID,
                operationId = operationId,
                maxAttempts = 3,
                pollInterval = Duration.ZERO,
            )

        assertEquals(TelegramBetOperationState.PENDING, result)
        verify(gateway, times(3)).getOperationState(TELEGRAM_ID, operationId)
    }

    private companion object {
        const val TELEGRAM_ID = 931458258L
    }
}
