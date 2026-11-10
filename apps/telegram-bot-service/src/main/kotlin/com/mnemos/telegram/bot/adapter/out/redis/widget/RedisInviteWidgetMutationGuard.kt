package com.mnemos.telegram.bot.adapter.out.redis.widget

import com.mnemos.telegram.bot.application.widget.invites.port.out.InviteWidgetMutationGuard
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
class RedisInviteWidgetMutationGuard(
    private val redis: StringRedisTemplate,
    private val bot: TelegramBotProperties,
) : InviteWidgetMutationGuard {
    override fun claimQuery(telegramId: Long, queryId: String): Boolean {
        if (queryId.isBlank() || queryId.length > 512) return false
        val hash = HexFormat.of().formatHex(
            MessageDigest.getInstance("SHA-256").digest(queryId.toByteArray(StandardCharsets.UTF_8)),
        )
        return redis.opsForValue().setIfAbsent(
            "telegram-bot:widget-invite-action:" + bot.token.substringBefore(':') + ":" + telegramId + ":" + hash,
            "1",
            Duration.ofDays(1),
        ) == true
    }

    override fun requestRevocation(telegramId: Long, messageId: Int, inviteId: UUID) {
        redis.opsForValue().set(confirmationKey(telegramId, messageId), inviteId.toString(), Duration.ofMinutes(3))
    }

    override fun consumeRevocation(telegramId: Long, messageId: Int, inviteId: UUID): Boolean =
        redis.opsForValue().getAndDelete(confirmationKey(telegramId, messageId)) == inviteId.toString()

    override fun cancelRevocation(telegramId: Long, messageId: Int) {
        redis.delete(confirmationKey(telegramId, messageId))
    }

    private fun confirmationKey(telegramId: Long, messageId: Int): String =
        "telegram-bot:widget-invite-confirm:" + bot.token.substringBefore(':') + ":" + telegramId + ":" + messageId
}
