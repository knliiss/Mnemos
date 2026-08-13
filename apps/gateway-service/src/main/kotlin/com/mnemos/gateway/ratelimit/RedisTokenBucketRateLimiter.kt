package com.mnemos.gateway.ratelimit

import org.springframework.data.redis.core.ReactiveStringRedisTemplate
import org.springframework.data.redis.core.script.DefaultRedisScript
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono
import java.time.Clock
import kotlin.math.ceil

@Component
class RedisTokenBucketRateLimiter(
    private val redis: ReactiveStringRedisTemplate,
    private val clock: Clock,
) {
    private val script = DefaultRedisScript(SCRIPT, Long::class.java)

    fun isAllowed(
        bucketKey: String,
        rule: RateLimitRule,
    ): Mono<Boolean> {
        val refillPerMillisecond = rule.sustainedPerMinute.toDouble() / MILLIS_PER_MINUTE
        val capacity = rule.burstCapacity.toDouble()
        val ttlMillis = calculateTtlMillis(refillPerMillisecond, capacity)
        val now = clock.millis()

        return redis
            .execute(
                script,
                listOf("mnemos:gateway:rate-limit:$bucketKey"),
                listOf(
                    refillPerMillisecond.toString(),
                    capacity.toString(),
                    now.toString(),
                    ttlMillis.toString(),
                ),
            ).next()
            .map { allowed -> allowed == 1L }
            .defaultIfEmpty(false)
    }

    private fun calculateTtlMillis(
        refillPerMillisecond: Double,
        capacity: Double,
    ): Long {
        val refillDuration = ceil(capacity / refillPerMillisecond).toLong()
        return maxOf(MILLIS_PER_MINUTE.toLong(), refillDuration * 2)
    }

    private companion object {
        const val MILLIS_PER_MINUTE = 60_000.0

        val SCRIPT =
            """
            local values = redis.call('HMGET', KEYS[1], 'tokens', 'timestamp')
            local tokens = tonumber(values[1])
            local last_timestamp = tonumber(values[2])
            local refill_rate = tonumber(ARGV[1])
            local capacity = tonumber(ARGV[2])
            local now = tonumber(ARGV[3])
            local ttl = tonumber(ARGV[4])

            if tokens == nil then
                tokens = capacity
            end

            if last_timestamp == nil then
                last_timestamp = now
            end

            local elapsed = math.max(0, now - last_timestamp)
            local available = math.min(capacity, tokens + (elapsed * refill_rate))
            local allowed = 0

            if available >= 1 then
                allowed = 1
                available = available - 1
            end

            redis.call('HSET', KEYS[1], 'tokens', available, 'timestamp', now)
            redis.call('PEXPIRE', KEYS[1], ttl)

            return allowed
            """.trimIndent()
    }
}
