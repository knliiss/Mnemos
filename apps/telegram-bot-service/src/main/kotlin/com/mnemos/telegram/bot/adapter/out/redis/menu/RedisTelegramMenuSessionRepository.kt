package com.mnemos.telegram.bot.adapter.out.redis.menu

import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.ObjectMapper
import com.mnemos.telegram.bot.application.menu.model.TelegramMenuSession
import com.mnemos.telegram.bot.application.menu.port.out.TelegramMenuSessionRepository
import com.mnemos.telegram.bot.configuration.menu.TelegramMenuProperties
import org.slf4j.LoggerFactory
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.stereotype.Component

@Component
class RedisTelegramMenuSessionRepository(
    private val redisTemplate: StringRedisTemplate,
    private val objectMapper: ObjectMapper,
    private val properties: TelegramMenuProperties,
) : TelegramMenuSessionRepository {
    override fun find(telegramId: Long): TelegramMenuSession? {
        val key = key(telegramId)
        val encoded = redisTemplate.opsForValue().get(key) ?: return null

        return try {
            objectMapper.readValue(encoded, TelegramMenuSession::class.java)
        } catch (exception: JsonProcessingException) {
            log.warn("Discarding unreadable Telegram menu session for telegramId={}", telegramId, exception)
            redisTemplate.delete(key)
            null
        }
    }

    override fun save(session: TelegramMenuSession) {
        redisTemplate
            .opsForValue()
            .set(
                key(session.telegramId),
                objectMapper.writeValueAsString(session),
                properties.sessionTtl,
            )
    }

    private fun key(telegramId: Long): String = "$KEY_PREFIX:$telegramId"

    private companion object {
        const val KEY_PREFIX = "telegram-bot:menu"

        val log = LoggerFactory.getLogger(RedisTelegramMenuSessionRepository::class.java)
    }
}
