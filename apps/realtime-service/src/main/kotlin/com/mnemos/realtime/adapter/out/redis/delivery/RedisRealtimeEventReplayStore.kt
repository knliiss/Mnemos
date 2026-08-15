package com.mnemos.realtime.adapter.out.redis.delivery

import com.mnemos.realtime.adapter.redis.delivery.serialization.RedisRealtimeEventCodec
import com.mnemos.realtime.application.delivery.model.RealtimeEvent
import com.mnemos.realtime.application.delivery.port.out.RealtimeEventReplayStore
import com.mnemos.realtime.configuration.delivery.RealtimeDeliveryProperties
import org.slf4j.LoggerFactory
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.data.redis.core.script.DefaultRedisScript
import org.springframework.stereotype.Repository
import java.time.Clock
import java.util.UUID

@Repository
class RedisRealtimeEventReplayStore(
    private val redisTemplate: StringRedisTemplate,
    private val codec: RedisRealtimeEventCodec,
    private val properties: RealtimeDeliveryProperties,
    private val clock: Clock,
) : RealtimeEventReplayStore {
    override fun append(event: RealtimeEvent) {
        val key = replayKey(event.userId)
        val serialized = codec.encode(event)

        val appended =
            redisTemplate.execute(
                appendScript,
                listOf(key),
                serialized,
                (properties.replayLimit - 1).toString(),
                properties.replayTtl.toMillis().toString(),
            )

        check(appended == 1L) {
            "Redis did not acknowledge the realtime replay append"
        }
    }

    override fun findAfter(
        userId: UUID,
        lastEventId: UUID,
    ): List<RealtimeEvent> {
        val events =
            redisTemplate
                .opsForList()
                .range(
                    replayKey(userId),
                    0,
                    properties.replayLimit - 1,
                ).orEmpty()
                .mapNotNull(::decodeOrNull)
                .filter { event -> !event.occurredAt.isBefore(replayCutoff) }
                .asReversed()
                .distinctBy(RealtimeEvent::id)

        val lastIndex = events.indexOfLast { event -> event.id == lastEventId }

        return if (lastIndex < 0) {
            events
        } else {
            events.drop(lastIndex + 1)
        }
    }

    private val replayCutoff
        get() = clock.instant().minus(properties.replayTtl)

    private fun decodeOrNull(serialized: String): RealtimeEvent? =
        try {
            codec.decode(serialized)
        } catch (exception: Exception) {
            log.warn("Ignoring an unreadable realtime replay event", exception)
            null
        }

    private fun replayKey(userId: UUID): String = "realtime:sse:replay:$userId"

    private companion object {
        val log = LoggerFactory.getLogger(RedisRealtimeEventReplayStore::class.java)

        val appendScript =
            DefaultRedisScript(
                """
                redis.call('LREM', KEYS[1], 0, ARGV[1])
                redis.call('LPUSH', KEYS[1], ARGV[1])
                redis.call('LTRIM', KEYS[1], 0, ARGV[2])
                redis.call('PEXPIRE', KEYS[1], ARGV[3])
                return 1
                """.trimIndent(),
                Long::class.java,
            )
    }
}
