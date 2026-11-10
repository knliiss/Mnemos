package com.mnemos.telegram.bot.adapter.out.redis.widget

import com.mnemos.telegram.bot.configuration.telegram.TelegramBotProperties
import com.mnemos.telegram.bot.configuration.widget.TelegramWidgetProperties
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.mockito.kotlin.verify
import java.util.UUID
import com.mnemos.telegram.bot.application.widget.input.WidgetTextInputSession
import com.mnemos.telegram.bot.application.widget.input.WidgetTextInputPurpose
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
    fun roleEditPromptPersistsTargetRoleIdentityInVersionedSession() {
        val accountId = UUID.randomUUID()
        val roleId = UUID.randomUUID()
        val data = WidgetTextInputSession(11L, 41, 8, accountId, WidgetTextInputPurpose.ACCOUNT_ROLE_EDIT, roleId)
        val key = "telegram-bot:widget-input:12345:11:41"
        whenever(redis.opsForValue()).thenReturn(values)

        repository.save(data)
        verify(values).set(
            eq(key),
            eq("v2|8|$accountId|ACCOUNT_ROLE_EDIT|$roleId"),
            eq(Duration.ofMinutes(10)),
        )
        whenever(values.get(key)).thenReturn("v2|8|$accountId|ACCOUNT_ROLE_EDIT|$roleId")
        assertEquals(data, repository.find(11L, 41))
    }

    @Test
    fun oldV1SearchPromptRemainsReadableAfterUpgrade() {
        val key = "telegram-bot:widget-input:12345:11:41"
        whenever(redis.opsForValue()).thenReturn(values)
        whenever(values.get(key)).thenReturn("v1|8|-|EVENT_SEARCH")
        assertEquals(
            WidgetTextInputSession(11L, 41, 8, null, WidgetTextInputPurpose.EVENT_SEARCH),
            repository.find(11L, 41),
        )
    }

    @Test
    fun roleEditPromptWithoutPersistedRoleIsRejected() {
        val key = "telegram-bot:widget-input:12345:11:41"
        whenever(redis.opsForValue()).thenReturn(values)
        whenever(values.get(key)).thenReturn("v1|8|-|ACCOUNT_ROLE_EDIT")
        assertEquals(null, repository.find(11L, 41))
    }

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
