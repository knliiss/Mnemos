package com.mnemos.notification.adapter.out.redis.ping

import com.mnemos.notification.application.ping.port.out.PingDeduplicationStore
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.stereotype.Component
import java.nio.charset.StandardCharsets
import java.time.Duration
import java.util.UUID

@Component
class RedisPingDeduplicationStore(
    private val redisTemplate: StringRedisTemplate,
) : PingDeduplicationStore {
    override fun accept(
        sender: String,
        message: String,
    ): Boolean {
        val fingerprint =
            UUID.nameUUIDFromBytes(
                (sender.lowercase() + "\n" + message).toByteArray(StandardCharsets.UTF_8),
            )
        val key = "notification:{delivery}:ping:dedup:" + fingerprint

        return redisTemplate.opsForValue().setIfAbsent(key, "1", TTL) == true
    }

    private companion object {
        val TTL: Duration = Duration.ofSeconds(3)
    }
}
