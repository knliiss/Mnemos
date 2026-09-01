package com.mnemos.telegram.bot.adapter.out.redis.menu

import com.mnemos.telegram.bot.application.menu.model.TelegramMenuScreen
import com.mnemos.telegram.bot.application.menu.model.TelegramMenuSession
import com.mnemos.telegram.bot.application.menu.model.TelegramPendingInput
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test

class TelegramMenuSessionRedisCodecTest {
    @Test
    fun `session round trip preserves navigation input and context`() {
        val session =
            TelegramMenuSession(
                telegramId = 931458258L,
                menuMessageId = 42,
                currentScreen = TelegramMenuScreen.ONLINE,
                navigationStack = listOf(TelegramMenuScreen.HOME),
                pendingInput = TelegramPendingInput.ONLINE_PLAYER_SEARCH,
                screenContext =
                    mapOf(
                        "query" to "Sword Art, Online: α",
                        "page" to "2",
                    ),
            )

        val decoded = TelegramMenuSessionRedisCodec.decode(TelegramMenuSessionRedisCodec.encode(session))

        assertEquals(session, decoded)
    }

    @Test
    fun `version one session remains readable without pending input`() {
        val encodedVersionOne = "1\n931458258\n42\nONLINE\nHOME\n"

        val decoded = TelegramMenuSessionRedisCodec.decode(encodedVersionOne)

        assertEquals(931458258L, decoded?.telegramId)
        assertEquals(42, decoded?.menuMessageId)
        assertEquals(TelegramMenuScreen.ONLINE, decoded?.currentScreen)
        assertEquals(listOf(TelegramMenuScreen.HOME), decoded?.navigationStack)
        assertNull(decoded?.pendingInput)
        assertEquals(emptyMap<String, String>(), decoded?.screenContext)
    }

    @Test
    fun `persisted pending input for another screen is rejected`() {
        val encoded = "2\n931458258\n42\nEVENTS\nHOME\nACCOUNT_CREATE\n"

        val decoded = TelegramMenuSessionRedisCodec.decode(encoded)

        assertNull(decoded)
    }

    @Test
    fun `invalid persisted session is rejected`() {
        assertNull(TelegramMenuSessionRedisCodec.decode("broken"))
    }
}
