package com.mnemos.telegram.bot.adapter.out.redis.menu

import com.mnemos.telegram.bot.application.menu.model.TelegramMenuSession
import com.mnemos.telegram.bot.application.menu.port.out.TelegramMenuSessionRepository
import com.mnemos.telegram.bot.configuration.menu.TelegramMenuProperties
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.stereotype.Component

@Component
@ConditionalOnProperty(prefix = "mnemos.telegram-bot.telegram", name = ["enabled"], havingValue = "true")
class RedisTelegramMenuSessionRepository(
    private val redisTemplate: StringRedisTemplate,
    private val properties: TelegramMenuProperties,
) : TelegramMenuSessionRepository {
    override fun find(telegramId: Long): TelegramMenuSession? {
        val key = key(telegramId)
        val encoded = redisTemplate.opsForValue().get(key) ?: return null
        val session = TelegramMenuSessionRedisCodec.decode(encoded)

        if (session == null || session.telegramId != telegramId) {
            log.warn("Discarding unreadable Telegram menu session for telegramId={}", telegramId)
            redisTemplate.delete(key)
            return null
        }

        return session
    }

    override fun save(session: TelegramMenuSession) {
        redisTemplate
            .opsForValue()
            .set(
                key(session.telegramId),
                TelegramMenuSessionRedisCodec.encode(session),
                properties.sessionTtl,
            )
    }

    private fun key(telegramId: Long): String = "$KEY_PREFIX:$telegramId"

    private companion object {
        const val KEY_PREFIX = "telegram-bot:menu"

        val log = LoggerFactory.getLogger(RedisTelegramMenuSessionRepository::class.java)
    }
}
