package com.mnemos.realtime.adapter.sse.connection

import com.mnemos.realtime.configuration.delivery.RealtimeDeliveryProperties
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.data.redis.core.script.DefaultRedisScript
import org.springframework.stereotype.Component
import java.nio.charset.StandardCharsets
import java.util.Base64
import java.util.UUID

@Component
class RedisSseConnectionLeaseStore(
    private val redisTemplate: StringRedisTemplate,
    private val properties: RealtimeDeliveryProperties,
) : SseConnectionLeaseStore {
    override fun acquire(
        userId: UUID,
        clientIp: String,
        connectionId: UUID,
    ): SseConnectionLeaseAcquireResult =
        try {
            acquireFromRedis(
                userId = userId,
                clientIp = clientIp,
                connectionId = connectionId,
            )
        } catch (exception: Exception) {
            throw RealtimeConnectionCapacityUnavailableException(exception)
        }

    override fun renew(
        userId: UUID,
        clientIp: String,
        connectionId: UUID,
    ): Boolean {
        val result =
            redisTemplate.execute(
                RENEW_SCRIPT,
                keys(userId, clientIp),
                properties.connectionLeaseTtl.toMillis().toString(),
                connectionId.toString(),
                leaseKeyTtlSeconds().toString(),
            ) ?: 0L

        return result == 1L
    }

    override fun release(
        userId: UUID,
        clientIp: String,
        connectionId: UUID,
    ) {
        redisTemplate.execute(
            RELEASE_SCRIPT,
            keys(userId, clientIp),
            connectionId.toString(),
        )
    }

    private fun acquireFromRedis(
        userId: UUID,
        clientIp: String,
        connectionId: UUID,
    ): SseConnectionLeaseAcquireResult {
        val result =
            redisTemplate.execute(
                ACQUIRE_SCRIPT,
                keys(userId, clientIp),
                properties.connectionLeaseTtl.toMillis().toString(),
                connectionId.toString(),
                properties.maxConnectionsPerUser.toString(),
                properties.maxConnectionsPerClientIp.toString(),
                properties.maxConnectionsTotal.toString(),
                leaseKeyTtlSeconds().toString(),
            ) ?: error("Redis did not return an SSE connection lease result")

        return when (result) {
            0L -> SseConnectionLeaseAcquireResult.ACQUIRED
            1L -> SseConnectionLeaseAcquireResult.USER_LIMIT_EXCEEDED
            2L -> SseConnectionLeaseAcquireResult.CLIENT_IP_LIMIT_EXCEEDED
            3L -> SseConnectionLeaseAcquireResult.TOTAL_LIMIT_EXCEEDED
            else -> error("Unknown SSE connection lease result: $result")
        }
    }

    private fun keys(
        userId: UUID,
        clientIp: String,
    ): List<String> =
        listOf(
            "$KEY_PREFIX:all",
            "$KEY_PREFIX:user:$userId",
            "$KEY_PREFIX:ip:${encodeClientIp(clientIp)}",
        )

    private fun encodeClientIp(clientIp: String): String =
        Base64
            .getUrlEncoder()
            .withoutPadding()
            .encodeToString(clientIp.toByteArray(StandardCharsets.UTF_8))

    private fun leaseKeyTtlSeconds(): Long =
        properties.connectionLeaseTtl
            .multipliedBy(2)
            .seconds
            .coerceAtLeast(1)

    private companion object {
        const val KEY_PREFIX = "mnemos:realtime:sse:leases"

        val ACQUIRE_SCRIPT =
            DefaultRedisScript(
                """
                local leaseTtlMillis = tonumber(ARGV[1])
                local connectionId = ARGV[2]
                local userLimit = tonumber(ARGV[3])
                local ipLimit = tonumber(ARGV[4])
                local totalLimit = tonumber(ARGV[5])
                local keyTtlSeconds = tonumber(ARGV[6])
                local time = redis.call('TIME')
                local now = (time[1] * 1000) + math.floor(time[2] / 1000)
                local expiresAt = now + leaseTtlMillis

                for index = 1, 3 do
                    redis.call('ZREMRANGEBYSCORE', KEYS[index], '-inf', now)
                end

                if redis.call('ZCARD', KEYS[2]) >= userLimit then
                    return 1
                end

                if redis.call('ZCARD', KEYS[3]) >= ipLimit then
                    return 2
                end

                if redis.call('ZCARD', KEYS[1]) >= totalLimit then
                    return 3
                end

                for index = 1, 3 do
                    redis.call('ZADD', KEYS[index], expiresAt, connectionId)
                    redis.call('EXPIRE', KEYS[index], keyTtlSeconds)
                end

                return 0
                """.trimIndent(),
                Long::class.java,
            )

        val RENEW_SCRIPT =
            DefaultRedisScript(
                """
                local leaseTtlMillis = tonumber(ARGV[1])
                local connectionId = ARGV[2]
                local keyTtlSeconds = tonumber(ARGV[3])

                if redis.call('ZSCORE', KEYS[1], connectionId) == false then
                    return 0
                end

                local time = redis.call('TIME')
                local now = (time[1] * 1000) + math.floor(time[2] / 1000)
                local expiresAt = now + leaseTtlMillis

                for index = 1, 3 do
                    redis.call('ZADD', KEYS[index], expiresAt, connectionId)
                    redis.call('EXPIRE', KEYS[index], keyTtlSeconds)
                end

                return 1
                """.trimIndent(),
                Long::class.java,
            )

        val RELEASE_SCRIPT =
            DefaultRedisScript(
                """
                local connectionId = ARGV[1]

                for index = 1, 3 do
                    redis.call('ZREM', KEYS[index], connectionId)
                end

                return 1
                """.trimIndent(),
                Long::class.java,
            )
    }
}
