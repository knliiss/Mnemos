package com.mnemos.telegram.bot.adapter.out.redis.widget

import com.mnemos.telegram.bot.configuration.telegram.TelegramBotProperties
import com.mnemos.telegram.bot.configuration.widget.TelegramWidgetProperties
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.data.redis.core.ValueOperations
import java.time.Duration

class RedisWidgetTextInputSessionRepositoryTest {
    private val redis = mock<StringRedisTemplate>()
    private val values = mock<ValueOperations<String, String>>()
    private val repository = RedisWidgetTextInputSessionRepository(
        redis,
        TelegramBotProperties(token = "12345:testing"),
        TelegramWidgetProperties(sessionTtl = Duration.ofDays(7)),
    )

    @Test
    fun onlyOneDeliveryCanClaimTheSameAccountMutationPrompt() {
        val key = "telegram-bot:widget-input:12345:11:41:claimed"
        whenever(redis.opsForValue()).thenReturn(values)
        whenever(values.setIfAbsent(eq(key), eq("1"), eq(Duration.ofMinutes(10))))
            .thenReturn(true, false)

        assertTrue(repository.claimMutation(11L, 41))
        assertFalse(repository.claimMutation(11L, 41))
    }
}
