package com.mnemos.realtime.adapter.out.redis.broadcast

import com.mnemos.realtime.application.broadcast.model.DropSpotlightPresentationMode
import com.mnemos.realtime.application.broadcast.port.out.DropSpotlightPresentationPreferenceStore
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.stereotype.Component
import java.util.UUID

@Component
class RedisDropSpotlightPresentationPreferenceStore(
    private val redisTemplate: StringRedisTemplate,
) : DropSpotlightPresentationPreferenceStore {
    override fun find(userId: UUID): DropSpotlightPresentationMode {
        val stored = redisTemplate.opsForHash<String, String>().get(PREFERENCES_KEY, userId.toString())

        return stored
            ?.let { value -> runCatching { DropSpotlightPresentationMode.valueOf(value) }.getOrNull() }
            ?: DropSpotlightPresentationMode.FULL
    }

    override fun save(
        userId: UUID,
        mode: DropSpotlightPresentationMode,
    ) {
        redisTemplate
            .opsForHash<String, String>()
            .put(PREFERENCES_KEY, userId.toString(), mode.name)
    }

    private companion object {
        const val PREFERENCES_KEY = "realtime:drop-spotlight:presentation-preferences:v1"
    }
}
