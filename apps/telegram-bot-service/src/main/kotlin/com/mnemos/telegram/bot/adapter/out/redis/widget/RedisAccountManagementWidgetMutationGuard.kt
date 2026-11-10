package com.mnemos.telegram.bot.adapter.out.redis.widget

import com.mnemos.telegram.bot.application.widget.management.port.out.AccountManagementWidgetMutationGuard
import com.mnemos.telegram.bot.configuration.telegram.TelegramBotProperties
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.stereotype.Component
import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import java.time.Duration
import java.util.HexFormat
import java.util.UUID

@Component
@ConditionalOnProperty(prefix = "mnemos.telegram-bot.telegram", name = ["enabled"], havingValue = "true")
class RedisAccountManagementWidgetMutationGuard(
    private val redis: StringRedisTemplate,
    private val bot: TelegramBotProperties,
) : AccountManagementWidgetMutationGuard {
    override fun claimQuery(telegramId: Long, queryId: String): Boolean {
        if (telegramId <= 0 || queryId.isBlank() || queryId.length > 512) return false
        val digest = MessageDigest.getInstance("SHA-256")
            .digest(queryId.toByteArray(StandardCharsets.UTF_8))
        val hash = HexFormat.of().formatHex(digest)
        return redis.opsForValue().setIfAbsent(
            "telegram-bot:widget-management-action:" + bot.token.substringBefore(':') + ":" + telegramId + ":" + hash,
            "1",
            Duration.ofDays(1),
        ) == true
    }

    override fun requestKick(telegramId: Long, messageId: Int, accountId: UUID, memberId: UUID) {
        redis.opsForValue().set(
            kickKey(telegramId, messageId),
            "$accountId:$memberId",
            Duration.ofMinutes(3),
        )
    }

    override fun consumeKick(telegramId: Long, messageId: Int, accountId: UUID, memberId: UUID): Boolean =
        redis.opsForValue().getAndDelete(kickKey(telegramId, messageId)) == "$accountId:$memberId"

    override fun cancelKick(telegramId: Long, messageId: Int) {
        redis.delete(kickKey(telegramId, messageId))
    }

    private fun kickKey(telegramId: Long, messageId: Int): String =
        "telegram-bot:widget-management-confirm:" + bot.token.substringBefore(':') + ":" + telegramId + ":" + messageId
}
