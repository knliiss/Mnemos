package com.mnemos.realtime.adapter.out.redis.collector

import com.mnemos.realtime.application.collector.model.CollectorObservationUpdateResult
import com.mnemos.realtime.application.collector.model.CollectorPresenceSnapshot
import com.mnemos.realtime.application.collector.model.CollectorUpdateSlotDecision
import com.mnemos.realtime.application.collector.port.out.CollectorSessionLeaseStore
import com.mnemos.realtime.domain.collector.model.CollectorCredentialId
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.data.redis.core.script.DefaultRedisScript
import org.springframework.stereotype.Component
import java.time.Duration
import java.util.UUID

@Component
class RedisCollectorSessionLeaseStore(
    private val redisTemplate: StringRedisTemplate,
) : CollectorSessionLeaseStore {
    override fun acquire(
        collectorId: CollectorCredentialId,
        sessionToken: UUID,
        ttl: Duration,
    ) {
        val acquired =
            redisTemplate.execute(
                acquireScript,
                listOf(
                    leaseKey(collectorId),
                    ACTIVE_COLLECTORS_KEY,
                ),
                sessionToken.toString(),
                ttl.toMillis().toString(),
                collectorId.id.toString(),
            )

        check(acquired == 1L) {
            "Redis did not acknowledge the collector lease acquisition"
        }
    }

    override fun renew(
        collectorId: CollectorCredentialId,
        sessionToken: UUID,
        ttl: Duration,
    ): Boolean =
        redisTemplate.execute(
            renewScript,
            listOf(leaseKey(collectorId)),
            sessionToken.toString(),
            ttl.toMillis().toString(),
        ) == 1L

    override fun markObserving(
        collectorId: CollectorCredentialId,
        sessionToken: UUID,
        ttl: Duration,
    ): CollectorObservationUpdateResult =
        observationUpdateResult(
            redisTemplate.execute(
                markObservingScript,
                listOf(
                    leaseKey(collectorId),
                    ACTIVE_COLLECTORS_KEY,
                ),
                sessionToken.toString(),
                ttl.toMillis().toString(),
                collectorId.id.toString(),
            ),
        )

    override fun renewObservation(
        collectorId: CollectorCredentialId,
        sessionToken: UUID,
        ttl: Duration,
    ): Boolean =
        redisTemplate.execute(
            renewObservationScript,
            listOf(
                leaseKey(collectorId),
                ACTIVE_COLLECTORS_KEY,
            ),
            sessionToken.toString(),
            ttl.toMillis().toString(),
            collectorId.id.toString(),
        ) == 1L

    override fun markPaused(
        collectorId: CollectorCredentialId,
        sessionToken: UUID,
    ): CollectorObservationUpdateResult =
        observationUpdateResult(
            redisTemplate.execute(
                markPausedScript,
                listOf(
                    leaseKey(collectorId),
                    ACTIVE_COLLECTORS_KEY,
                ),
                sessionToken.toString(),
                collectorId.id.toString(),
            ),
        )

    override fun requestUpdateSlot(
        collectorId: CollectorCredentialId,
        sessionToken: UUID,
        version: String,
        slotTtl: Duration,
    ): CollectorUpdateSlotDecision? {
        val encoded =
            requireNotNull(
                redisTemplate.execute(
                    requestUpdateSlotScript,
                    listOf(
                        leaseKey(collectorId),
                        ACTIVE_COLLECTORS_KEY,
                        UPDATE_SLOT_KEY,
                    ),
                    sessionToken.toString(),
                    collectorId.id.toString(),
                    version,
                    slotTtl.toMillis().toString(),
                ),
            ) {
                "Redis did not return a collector update slot decision"
            }

        if (encoded == UPDATE_SESSION_NOT_OWNED) {
            return null
        }

        val parts = encoded.split(':', limit = 2)
        val status = parts.first()
        val retryAfterSeconds = parts.getOrNull(1)?.toLongOrNull()

        return when (status) {
            UPDATE_GRANTED ->
                CollectorUpdateSlotDecision(
                    granted = true,
                    retryAfterSeconds = null,
                )

            UPDATE_DEFERRED ->
                CollectorUpdateSlotDecision(
                    granted = false,
                    retryAfterSeconds = requireNotNull(retryAfterSeconds),
                )

            else -> error("Redis returned an unknown collector update slot decision: $encoded")
        }
    }

    override fun release(
        collectorId: CollectorCredentialId,
        sessionToken: UUID,
    ): Boolean =
        redisTemplate.execute(
            releaseScript,
            listOf(
                leaseKey(collectorId),
                ACTIVE_COLLECTORS_KEY,
            ),
            sessionToken.toString(),
            collectorId.id.toString(),
        ) == 1L

    override fun revoke(collectorId: CollectorCredentialId) {
        val revoked =
            redisTemplate.execute(
                revokeScript,
                listOf(
                    leaseKey(collectorId),
                    ACTIVE_COLLECTORS_KEY,
                ),
                collectorId.id.toString(),
            )

        check(revoked == 1L) {
            "Redis did not acknowledge the collector lease revocation"
        }
    }

    override fun snapshot(): CollectorPresenceSnapshot {
        val encoded =
            requireNotNull(
                redisTemplate.execute(
                    snapshotScript,
                    listOf(
                        ACTIVE_COLLECTORS_KEY,
                        PRESENCE_COUNT_KEY,
                        PRESENCE_REVISION_KEY,
                    ),
                ),
            ) {
                "Redis did not return a collector presence snapshot"
            }
        val (count, revision, activeCollectorIds) = encoded.split(':', limit = 3)

        return CollectorPresenceSnapshot(
            activeCollectors = count.toInt(),
            revision = revision.toLong(),
            activeCollectorIds =
                activeCollectorIds
                    .takeIf { it.isNotBlank() }
                    ?.split(',')
                    ?.mapTo(mutableSetOf()) { CollectorCredentialId(UUID.fromString(it)) }
                    ?: emptySet(),
        )
    }

    private fun leaseKey(collectorId: CollectorCredentialId): String = "$LEASE_KEY_PREFIX:${collectorId.id}"

    private fun observationUpdateResult(result: Long?): CollectorObservationUpdateResult =
        when (result) {
            OBSERVATION_CHANGED -> CollectorObservationUpdateResult.CHANGED
            OBSERVATION_UNCHANGED -> CollectorObservationUpdateResult.UNCHANGED
            SESSION_NOT_OWNED -> CollectorObservationUpdateResult.SESSION_NOT_OWNED
            else -> error("Redis returned an unknown collector observation result: $result")
        }

    private companion object {
        const val ACTIVE_COLLECTORS_KEY = "realtime:{collectors}:active"
        const val LEASE_KEY_PREFIX = "realtime:{collectors}:lease"
        const val PRESENCE_COUNT_KEY = "realtime:{collectors}:presence:count"
        const val PRESENCE_REVISION_KEY = "realtime:{collectors}:presence:revision"
        const val UPDATE_SLOT_KEY = "realtime:{collectors}:update:slot"

        const val SESSION_NOT_OWNED = -1L
        const val OBSERVATION_UNCHANGED = 0L
        const val OBSERVATION_CHANGED = 1L

        const val UPDATE_SESSION_NOT_OWNED = "NOT_OWNED"
        const val UPDATE_GRANTED = "GRANTED"
        const val UPDATE_DEFERRED = "DEFERRED"

        val acquireScript =
            DefaultRedisScript(
                """
                redis.call('SET', KEYS[1], ARGV[1], 'PX', ARGV[2])
                redis.call('ZREM', KEYS[2], ARGV[3])
                return 1
                """.trimIndent(),
                Long::class.java,
            )

        val renewScript =
            DefaultRedisScript(
                """
                if redis.call('GET', KEYS[1]) ~= ARGV[1] then
                    return 0
                end

                redis.call('PEXPIRE', KEYS[1], ARGV[2])
                return 1
                """.trimIndent(),
                Long::class.java,
            )

        val markObservingScript =
            DefaultRedisScript(
                """
                if redis.call('GET', KEYS[1]) ~= ARGV[1] then
                    return -1
                end

                local time = redis.call('TIME')
                local now = (time[1] * 1000) + math.floor(time[2] / 1000)
                local expires_at = now + tonumber(ARGV[2])
                local previous_expiry = redis.call('ZSCORE', KEYS[2], ARGV[3])
                local changed = 1

                if previous_expiry and tonumber(previous_expiry) > now then
                    changed = 0
                end

                redis.call('ZADD', KEYS[2], expires_at, ARGV[3])
                return changed
                """.trimIndent(),
                Long::class.java,
            )

        val renewObservationScript =
            DefaultRedisScript(
                """
                if redis.call('GET', KEYS[1]) ~= ARGV[1] then
                    return 0
                end

                local time = redis.call('TIME')
                local now = (time[1] * 1000) + math.floor(time[2] / 1000)
                local previous_expiry = redis.call('ZSCORE', KEYS[2], ARGV[3])

                if not previous_expiry or tonumber(previous_expiry) <= now then
                    redis.call('ZREM', KEYS[2], ARGV[3])
                    return 0
                end

                local expires_at = now + tonumber(ARGV[2])

                redis.call('ZADD', KEYS[2], expires_at, ARGV[3])
                return 1
                """.trimIndent(),
                Long::class.java,
            )

        val markPausedScript =
            DefaultRedisScript(
                """
                if redis.call('GET', KEYS[1]) ~= ARGV[1] then
                    return -1
                end

                return redis.call('ZREM', KEYS[2], ARGV[2])
                """.trimIndent(),
                Long::class.java,
            )

        val requestUpdateSlotScript =
            DefaultRedisScript(
                """
                if redis.call('GET', KEYS[1]) ~= ARGV[1] then
                    return 'NOT_OWNED'
                end

                local time = redis.call('TIME')
                local now = (time[1] * 1000) + math.floor(time[2] / 1000)

                redis.call('ZREMRANGEBYSCORE', KEYS[2], '-inf', now)

                local requested_slot = ARGV[2] .. '|' .. ARGV[3]
                local current_slot = redis.call('GET', KEYS[3])

                if current_slot then
                    if current_slot == requested_slot then
                        return 'GRANTED:0'
                    end

                    local remaining = redis.call('PTTL', KEYS[3])
                    local retry_after = math.max(1, math.ceil(remaining / 1000))

                    return 'DEFERRED:' .. tostring(retry_after)
                end

                redis.call('SET', KEYS[3], requested_slot, 'PX', ARGV[4])
                return 'GRANTED:0'
                """.trimIndent(),
                String::class.java,
            )

        val releaseScript =
            DefaultRedisScript(
                """
                if redis.call('GET', KEYS[1]) ~= ARGV[1] then
                    return 0
                end

                redis.call('DEL', KEYS[1])
                redis.call('ZREM', KEYS[2], ARGV[2])
                return 1
                """.trimIndent(),
                Long::class.java,
            )

        val revokeScript =
            DefaultRedisScript(
                """
                redis.call('DEL', KEYS[1])
                redis.call('ZREM', KEYS[2], ARGV[1])
                return 1
                """.trimIndent(),
                Long::class.java,
            )

        val snapshotScript =
            DefaultRedisScript(
                """
                local time = redis.call('TIME')
                local now = (time[1] * 1000) + math.floor(time[2] / 1000)

                redis.call('ZREMRANGEBYSCORE', KEYS[1], '-inf', now)

                local count = redis.call('ZCARD', KEYS[1])
                local previous = redis.call('GET', KEYS[2])
                local revision = tonumber(redis.call('GET', KEYS[3]) or '0')

                if previous ~= tostring(count) then
                    revision = math.max(revision + 1, now)
                    redis.call('SET', KEYS[3], string.format('%.0f', revision))
                    redis.call('SET', KEYS[2], tostring(count))
                end

                local active_collector_ids = redis.call('ZRANGE', KEYS[1], 0, -1)

                return tostring(count)
                    .. ':' .. string.format('%.0f', revision)
                    .. ':' .. table.concat(active_collector_ids, ',')
                """.trimIndent(),
                String::class.java,
            )
    }
}
