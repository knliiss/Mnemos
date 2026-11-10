package com.mnemos.telegram.bot.adapter.out.redis.widget

import com.mnemos.telegram.bot.application.widget.session.TelegramWidgetScreen
import com.mnemos.telegram.bot.application.widget.session.TelegramWidgetSession
import com.mnemos.telegram.bot.configuration.telegram.TelegramBotProperties
import com.mnemos.telegram.bot.configuration.widget.TelegramWidgetProperties
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.data.redis.core.ValueOperations
import java.time.Duration
import java.util.UUID

class RedisTelegramWidgetSessionRepositoryTest {
    private val redis = mock<StringRedisTemplate>()
    private val values = mock<ValueOperations<String, String>>()
    private val repository = RedisTelegramWidgetSessionRepository(
        redis,
        TelegramBotProperties(token = "12345:testing"),
        TelegramWidgetProperties(sessionTtl = Duration.ofDays(7)),
    )
    private val key = "telegram-bot:widget:12345:11:8"

    @Test
    fun v4RoundTripPreservesUnicodeSearchAndCursor() {
        whenever(redis.opsForValue()).thenReturn(values)
        val session = TelegramWidgetSession(
            telegramId = 11L,
            messageId = 8,
            screen = TelegramWidgetScreen.EVENTS,
            accountId = null,
            cursor = UUID.randomUUID(),
            search = "Кіріто",
        )

        repository.save(session)
        val captured = argumentCaptor<String>()
        verify(values).set(eq(key), captured.capture(), eq(Duration.ofDays(7)))
        assertTrue(captured.firstValue.startsWith("v4|"))
        whenever(values.get(key)).thenReturn(captured.firstValue)

        assertEquals(session, repository.find(11L, 8))
    }

    @Test
    fun oldV3SessionsRemainReadableWithoutSearch() {
        whenever(redis.opsForValue()).thenReturn(values)
        whenever(values.get(key)).thenReturn("v3|11|ONLINE|-|2|-")

        assertEquals(
            TelegramWidgetSession(11L, 8, TelegramWidgetScreen.ONLINE, null, page = 2),
            repository.find(11L, 8),
        )
    }
}
