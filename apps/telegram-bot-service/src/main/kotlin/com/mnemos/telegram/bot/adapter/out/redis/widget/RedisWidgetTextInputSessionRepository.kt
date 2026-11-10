package com.mnemos.telegram.bot.adapter.out.redis.widget

import com.mnemos.telegram.bot.application.widget.input.WidgetTextInputPurpose
import com.mnemos.telegram.bot.application.widget.input.WidgetTextInputSession
import com.mnemos.telegram.bot.application.widget.input.port.out.WidgetTextInputSessionRepository
import com.mnemos.telegram.bot.configuration.telegram.TelegramBotProperties
import com.mnemos.telegram.bot.configuration.widget.TelegramWidgetProperties
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.stereotype.Component
import java.time.Duration
import java.util.UUID

@Component
@ConditionalOnProperty(prefix = "mnemos.telegram-bot.telegram", name = ["enabled"], havingValue = "true")
class RedisWidgetTextInputSessionRepository(
    private val redis: StringRedisTemplate,
    private val telegram: TelegramBotProperties,
    private val widgets: TelegramWidgetProperties,
) : WidgetTextInputSessionRepository {
    override fun find(telegramId: Long, promptMessageId: Int): WidgetTextInputSession? {
        val value = redis.opsForValue().get(key(telegramId, promptMessageId)) ?: return null
        val parts = value.split('|')
        if (parts.size != 4 || parts[0] != "v1") return null
        val widgetId = parts[1].toIntOrNull()?.takeIf { it > 0 } ?: return null
        val accountId = if (parts[2] == "-") null else runCatching { UUID.fromString(parts[2]) }.getOrNull()
        if (parts[2] != "-" && accountId == null) return null
        val purpose = runCatching { WidgetTextInputPurpose.valueOf(parts[3]) }.getOrNull() ?: return null
        return WidgetTextInputSession(telegramId, promptMessageId, widgetId, accountId, purpose)
    }

    override fun save(session: WidgetTextInputSession) {
        val value = listOf("v1", session.widgetMessageId, session.accountId ?: "-", session.purpose.name).joinToString("|")
        val ttl = minOf(widgets.sessionTtl, Duration.ofMinutes(10))
        redis.opsForValue().set(key(session.telegramId, session.promptMessageId), value, ttl)
    }

    override fun claimMutation(telegramId: Long, promptMessageId: Int): Boolean =
        redis.opsForValue().setIfAbsent(
            key(telegramId, promptMessageId) + ":claimed",
            "1",
            minOf(widgets.sessionTtl, Duration.ofMinutes(10)),
        ) == true

    override fun delete(telegramId: Long, promptMessageId: Int) {
        redis.delete(key(telegramId, promptMessageId))
    }

    private fun key(telegramId: Long, promptMessageId: Int): String {
        val botId = telegram.token.substringBefore(':')
        return "telegram-bot:widget-input:" + botId + ":" + telegramId + ":" + promptMessageId
    }
}
