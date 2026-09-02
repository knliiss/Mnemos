package com.mnemos.telegram.bot.application.bet

import com.mnemos.telegram.bot.application.bet.model.BetMenuFeedback
import com.mnemos.telegram.bot.application.bet.model.BetMenuMode
import com.mnemos.telegram.bot.application.bet.model.TelegramBetOperationState
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.util.UUID

class BetMenuContextOperationTest {
    @Test
    fun `pending operation survives refresh for later status checks`() {
        val operationId = UUID.randomUUID()
        val context =
            BetMenuContext.operationResult(
                mode = BetMenuMode.RACES,
                operationId = operationId,
                state = TelegramBetOperationState.PENDING,
            )

        val refreshed = BetMenuContext.refresh(context, BetMenuMode.RACES)

        assertEquals(BetMenuMode.RACES, BetMenuContext.mode(refreshed))
        assertEquals(operationId, BetMenuContext.operationId(refreshed))
        assertEquals(BetMenuFeedback.PENDING, BetMenuContext.feedback(refreshed))
    }

    @Test
    fun `unavailable operation remains attached for a later retry`() {
        val operationId = UUID.randomUUID()
        val context =
            BetMenuContext.operationResult(
                mode = BetMenuMode.ROUNDS,
                operationId = operationId,
                state = TelegramBetOperationState.UNAVAILABLE,
            )

        assertEquals(operationId, BetMenuContext.operationId(context))
        assertEquals(BetMenuFeedback.UNAVAILABLE, BetMenuContext.feedback(context))
    }

    @Test
    fun `terminal operation does not remain attached to menu context`() {
        val operationId = UUID.randomUUID()
        val context =
            BetMenuContext.operationResult(
                mode = BetMenuMode.ROUNDS,
                operationId = operationId,
                state = TelegramBetOperationState.COMPLETED,
            )

        assertEquals(null, BetMenuContext.operationId(context))
        assertEquals(BetMenuFeedback.COMPLETED, BetMenuContext.feedback(context))
    }
}
