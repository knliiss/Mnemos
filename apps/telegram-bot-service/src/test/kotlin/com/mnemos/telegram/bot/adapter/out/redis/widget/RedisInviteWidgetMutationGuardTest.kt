package com.mnemos.telegram.bot.adapter.out.redis.widget

import com.mnemos.telegram.bot.configuration.telegram.TelegramBotProperties
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.data.redis.core.ValueOperations
import java.time.Duration
import java.util.UUID

class RedisInviteWidgetMutationGuardTest {
    private val redis = mock<StringRedisTemplate>()
    private val values = mock<ValueOperations<String, String>>()
    private val guard = RedisInviteWidgetMutationGuard(redis, TelegramBotProperties(token = "12345:testing"))

    @Test
    fun claimsOnlyFirstDeliveryOfTheSameQuery() {
        whenever(redis.opsForValue()).thenReturn(values)
        whenever(values.setIfAbsent(any(), eq("1"), eq(Duration.ofDays(1)))).thenReturn(true, false)

        assertTrue(guard.claimQuery(11L, "query-1"))
        assertFalse(guard.claimQuery(11L, "query-1"))
    }

    @Test
    fun revocationMustMatchExactMessageScopedConfirmation() {
        val invite = UUID.randomUUID()
        val key = "telegram-bot:widget-invite-confirm:12345:11:8"
        whenever(redis.opsForValue()).thenReturn(values)
        whenever(values.getAndDelete(key)).thenReturn(invite.toString(), null)

        guard.requestRevocation(11L, 8, invite)
        verify(values).set(eq(key), eq(invite.toString()), eq(Duration.ofMinutes(3)))
        assertTrue(guard.consumeRevocation(11L, 8, invite))
        assertFalse(guard.consumeRevocation(11L, 8, invite))
    }
}
