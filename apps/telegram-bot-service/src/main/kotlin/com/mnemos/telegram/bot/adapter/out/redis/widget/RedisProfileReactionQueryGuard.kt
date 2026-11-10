package com.mnemos.telegram.bot.adapter.out.redis.widget

import com.mnemos.telegram.bot.application.widget.profiles.port.out.ProfileReactionQueryGuard
import com.mnemos.telegram.bot.configuration.telegram.TelegramBotProperties
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.stereotype.Component
import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import java.time.Duration
import java.util.HexFormat

@Component
@ConditionalOnProperty(prefix = "mnemos.telegram-bot.telegram", name = ["enabled"], havingValue = "true")
class RedisProfileReactionQueryGuard(
    private val redis: StringRedisTemplate,
    private val bot: TelegramBotProperties,
) : ProfileReactionQueryGuard {
    override fun claim(telegramId: Long, queryId: String): Boolean {
        if (telegramId <= 0 || queryId.isBlank() || queryId.length > 512) return false
        val digest = MessageDigest.getInstance("SHA-256")
            .digest(queryId.toByteArray(StandardCharsets.UTF_8))
        return redis.opsForValue().setIfAbsent(
            "telegram-bot:widget-profile-reaction:" + bot.token.substringBefore(':') +
                ":" + telegramId + ":" + HexFormat.of().formatHex(digest),
            "1",
            Duration.ofDays(1),
        ) == true
    }
}
