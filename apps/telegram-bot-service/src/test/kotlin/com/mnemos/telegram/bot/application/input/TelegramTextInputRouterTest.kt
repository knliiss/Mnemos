package com.mnemos.telegram.bot.application.input

import com.mnemos.telegram.bot.application.menu.model.TelegramMenuSession
import com.mnemos.telegram.bot.application.menu.model.TelegramPendingInput
import com.mnemos.telegram.bot.application.menu.port.out.TelegramMenuSessionRepository
import com.mnemos.telegram.bot.application.message.model.TelegramDeliveryResult
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class TelegramTextInputRouterTest {
    @Test
    fun `pending input is dispatched to matching handler`() {
        val repository = TestSessionRepository()
        val handler = TestInputHandler()
        val router = TelegramTextInputRouter(repository, listOf(handler))
        repository.save(
            TelegramMenuSession(
                telegramId = TELEGRAM_ID,
                pendingInput = TelegramPendingInput.ONLINE_PLAYER_SEARCH,
            ),
        )
        val context = TelegramTextInputContext(telegramId = TELEGRAM_ID, text = "knaliz")

        val result = router.route(context)

        assertEquals(TelegramDeliveryResult.Delivered, result)
        assertEquals(listOf(context), handler.handledContexts)
    }

    @Test
    fun `ordinary text without pending input is ignored`() {
        val repository = TestSessionRepository()
        val handler = TestInputHandler()
        val router = TelegramTextInputRouter(repository, listOf(handler))

        val result = router.route(TelegramTextInputContext(telegramId = TELEGRAM_ID, text = "hello"))

        assertEquals(TelegramDeliveryResult.Delivered, result)
        assertEquals(emptyList<TelegramTextInputContext>(), handler.handledContexts)
    }

    private class TestInputHandler : TelegramTextInputHandler {
        override val pendingInput: TelegramPendingInput = TelegramPendingInput.ONLINE_PLAYER_SEARCH

        val handledContexts = mutableListOf<TelegramTextInputContext>()

        override fun handle(context: TelegramTextInputContext): TelegramDeliveryResult {
            handledContexts += context
            return TelegramDeliveryResult.Delivered
        }
    }

    private class TestSessionRepository : TelegramMenuSessionRepository {
        private val sessions = mutableMapOf<Long, TelegramMenuSession>()

        override fun find(telegramId: Long): TelegramMenuSession? = sessions[telegramId]

        override fun save(session: TelegramMenuSession) {
            sessions[session.telegramId] = session
        }
    }

    private companion object {
        const val TELEGRAM_ID = 931458258L
    }
}
