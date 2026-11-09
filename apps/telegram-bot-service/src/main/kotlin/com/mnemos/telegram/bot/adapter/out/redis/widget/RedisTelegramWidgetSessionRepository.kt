package com.mnemos.telegram.bot.adapter.out.redis.widget

import com.mnemos.telegram.bot.application.widget.session.TelegramWidgetScreen
import com.mnemos.telegram.bot.application.widget.session.TelegramWidgetSession
import com.mnemos.telegram.bot.application.widget.session.port.out.TelegramWidgetSessionRepository
import com.mnemos.telegram.bot.configuration.telegram.TelegramBotProperties
import com.mnemos.telegram.bot.configuration.widget.TelegramWidgetProperties
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.stereotype.Component
import java.util.UUID

@Component
@ConditionalOnProperty(prefix = "mnemos.telegram-bot.telegram", name = ["enabled"], havingValue = "true")
class RedisTelegramWidgetSessionRepository(
    private val redis: StringRedisTemplate,
    private val telegramProperties: TelegramBotProperties,
    private val widgetProperties: TelegramWidgetProperties,
) : TelegramWidgetSessionRepository {
    override fun find(
        telegramId: Long,
        messageId: Int,
    ): TelegramWidgetSession? {
        val content = redis.opsForValue().get(key(telegramId, messageId)) ?: return null
        val parts = content.split('|')
        if ((parts[0] != "v2" || parts.size != 5) && (parts[0] != "v3" || parts.size != 6)) return null
        val owner = parts[1].toLongOrNull() ?: return null
        val screen = runCatching { TelegramWidgetScreen.valueOf(parts[2]) }.getOrNull() ?: return null
        val accountId = if (parts[3] == "-") null else runCatching { UUID.fromString(parts[3]) }.getOrNull()
        if (owner != telegramId || (parts[3] != "-" && accountId == null)) return null
        val page = parts[4].toIntOrNull()?.takeIf { it in 0..1_000 } ?: return null
        val cursor = if (parts.size == 6 && parts[5] != "-") runCatching { UUID.fromString(parts[5]) }.getOrNull() else null
        if (parts.size == 6 && parts[5] != "-" && cursor == null) return null
        return TelegramWidgetSession(telegramId, messageId, screen, accountId, page, cursor)
    }

    override fun save(session: TelegramWidgetSession) {
        val state = listOf("v3", session.telegramId, session.screen.name, session.accountId ?: "-", session.page, session.cursor ?: "-").joinToString("|")
        redis.opsForValue().set(key(session.telegramId, session.messageId), state, widgetProperties.sessionTtl)
    }

    private fun key(
        telegramId: Long,
        messageId: Int,
    ): String {
        val botId = telegramProperties.token.substringBefore(':')
        return "telegram-bot:widget:$botId:$telegramId:$messageId"
    }
}
