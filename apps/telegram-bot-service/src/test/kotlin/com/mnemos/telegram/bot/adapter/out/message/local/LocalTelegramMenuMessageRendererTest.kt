package com.mnemos.telegram.bot.adapter.out.message.local

import com.mnemos.telegram.bot.application.menu.model.TelegramMenuScreen
import com.mnemos.telegram.bot.application.menu.model.TelegramMenuScreenData
import com.mnemos.telegram.bot.application.message.model.OutboundTelegramMessageAction
import com.mnemos.telegram.bot.application.online.model.SaoOnlineRealm
import com.mnemos.telegram.bot.application.online.model.SaoOnlineSnapshot
import com.mnemos.telegram.bot.application.online.model.SaoOnlineSnapshotResult
import java.time.Instant
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class LocalTelegramMenuMessageRendererTest {
    private val renderer = LocalTelegramMenuMessageRenderer()

    @Test
    fun `online screen renders live realm snapshot and refresh action`() {
        val snapshot =
            SaoOnlineSnapshot(
                generatedAt = Instant.parse("2026-08-30T12:24:00Z"),
                trackedOnline = 58,
                realms =
                    listOf(
                        SaoOnlineRealm(
                            name = "SAO-1",
                            number = 1,
                            playerCount = 21,
                            players = emptyList(),
                        ),
                        SaoOnlineRealm(
                            name = "SAO-2",
                            number = 2,
                            playerCount = 37,
                            players = emptyList(),
                        ),
                    ),
            )
        val data =
            TelegramMenuScreenData.SaoOnline(
                SaoOnlineSnapshotResult.Available(snapshot),
            )

        val message = renderer.render(TelegramMenuScreen.ONLINE, data)
        val refreshAction = message.actions.first() as OutboundTelegramMessageAction.Callback

        assertTrue(message.text.contains("Сейчас отслеживается: <b>58</b>"))
        assertTrue(message.text.contains("SAO-1 — <b>21</b>"))
        assertTrue(message.text.contains("SAO-2 — <b>37</b>"))
        assertTrue(message.text.contains("30.08 12:24 UTC"))
        assertEquals("🔄 Обновить", refreshAction.label)
        assertEquals(TelegramMenuScreen.ONLINE.callbackData, refreshAction.data)
    }

    @Test
    fun `online screen renders recoverable unavailable state`() {
        val data = TelegramMenuScreenData.SaoOnline(SaoOnlineSnapshotResult.Unavailable)

        val message = renderer.render(TelegramMenuScreen.ONLINE, data)

        assertTrue(message.text.contains("Не удалось получить актуальный онлайн"))
        assertEquals(4, message.actions.size)
    }
}
