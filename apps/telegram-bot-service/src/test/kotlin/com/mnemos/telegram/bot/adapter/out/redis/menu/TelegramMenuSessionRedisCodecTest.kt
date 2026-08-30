package com.mnemos.telegram.bot.adapter.out.redis.menu

import com.mnemos.telegram.bot.application.menu.model.TelegramMenuScreen
import com.mnemos.telegram.bot.application.menu.model.TelegramMenuSession
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test

class TelegramMenuSessionRedisCodecTest {
    @Test
    fun `session round trip preserves navigation and context`() {
        val session =
            TelegramMenuSession(
                telegramId = 931458258L,
                menuMessageId = 42,
                currentScreen = TelegramMenuScreen.EVENTS,
                navigationStack =
                    listOf(
                        TelegramMenuScreen.HOME,
                        TelegramMenuScreen.ONLINE,
                    ),
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
    fun `invalid persisted session is rejected`() {
        assertNull(TelegramMenuSessionRedisCodec.decode("broken"))
    }
}
