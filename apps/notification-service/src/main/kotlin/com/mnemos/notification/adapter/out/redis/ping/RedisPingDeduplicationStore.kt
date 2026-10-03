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
    ): Boolean = redisTemplate.opsForValue().setIfAbsent(key(sender, message), "1", TTL) == true

    override fun release(
        sender: String,
        message: String,
    ) {
        redisTemplate.delete(key(sender, message))
    }

    private fun key(
        sender: String,
        message: String,
    ): String {
        val fingerprint =
            UUID.nameUUIDFromBytes(
                (sender.lowercase() + "\n" + message).toByteArray(StandardCharsets.UTF_8),
            )

        return "notification:{delivery}:ping:dedup:" + fingerprint
    }

    private companion object {
        val TTL: Duration = Duration.ofSeconds(3)
    }
}
