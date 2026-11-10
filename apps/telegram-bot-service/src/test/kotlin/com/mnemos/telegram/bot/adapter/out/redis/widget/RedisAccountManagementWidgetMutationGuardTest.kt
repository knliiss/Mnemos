package com.mnemos.telegram.bot.adapter.out.redis.widget

import com.mnemos.telegram.bot.configuration.telegram.TelegramBotProperties
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

class RedisAccountManagementWidgetMutationGuardTest {
    private val redis = mock<StringRedisTemplate>()
    private val values = mock<ValueOperations<String, String>>()
    private val guard = RedisAccountManagementWidgetMutationGuard(redis, TelegramBotProperties(token = "12345:testing"))

    @Test
    fun onlyFirstDeliveryOfQueryCanMutateData() {
        whenever(redis.opsForValue()).thenReturn(values)
        whenever(values.setIfAbsent(any(), eq("1"), eq(Duration.ofDays(1)))).thenReturn(true, false)

        assertTrue(guard.claimQuery(11L, "query-1"))
        assertFalse(guard.claimQuery(11L, "query-1"))
    }

    @Test
    fun roleDeletionConfirmationUsesSeparateAccountRoleScopedKey() {
        val accountId = UUID.randomUUID()
        val roleId = UUID.randomUUID()
        val key = "telegram-bot:widget-management-role-confirm:12345:11:8"
        whenever(redis.opsForValue()).thenReturn(values)
        whenever(values.getAndDelete(key)).thenReturn("$accountId:$roleId", null)

        guard.requestRoleDeletion(11L, 8, accountId, roleId)
        verify(values).set(eq(key), eq("$accountId:$roleId"), eq(Duration.ofMinutes(3)))
        assertTrue(guard.consumeRoleDeletion(11L, 8, accountId, roleId))
        assertFalse(guard.consumeRoleDeletion(11L, 8, accountId, roleId))
    }

    @Test
    fun kickConfirmationIsAccountMemberAndMessageBoundAndConsumedOnce() {
        val accountId = UUID.randomUUID()
        val memberId = UUID.randomUUID()
        val key = "telegram-bot:widget-management-confirm:12345:11:8"
        whenever(redis.opsForValue()).thenReturn(values)
        whenever(values.getAndDelete(key)).thenReturn("$accountId:$memberId", null)

        guard.requestKick(11L, 8, accountId, memberId)
        verify(values).set(eq(key), eq("$accountId:$memberId"), eq(Duration.ofMinutes(3)))
        assertTrue(guard.consumeKick(11L, 8, accountId, memberId))
        assertFalse(guard.consumeKick(11L, 8, accountId, memberId))
    }
}
