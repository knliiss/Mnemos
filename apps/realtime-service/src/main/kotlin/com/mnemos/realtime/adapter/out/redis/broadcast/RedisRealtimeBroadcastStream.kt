package com.mnemos.realtime.adapter.out.redis.broadcast

import com.fasterxml.jackson.databind.ObjectMapper
import com.mnemos.realtime.adapter.redis.broadcast.protocol.RealtimeBroadcastStreamContract
import com.mnemos.realtime.application.broadcast.model.RealtimeBroadcast
import com.mnemos.realtime.application.broadcast.port.out.RealtimeBroadcastFanout
import com.mnemos.realtime.configuration.delivery.RealtimeDeliveryProperties
import org.springframework.data.redis.connection.RedisStreamCommands
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.stereotype.Component

@Component
class RedisRealtimeBroadcastStream(
    private val redisTemplate: StringRedisTemplate,
    private val objectMapper: ObjectMapper,
    private val properties: RealtimeDeliveryProperties,
) : RealtimeBroadcastFanout {
    override fun publish(broadcast: RealtimeBroadcast) {
        val trimOptions = RedisStreamCommands.TrimOptions.maxLen(properties.streamMaxLength).approximate()
        val serialized = objectMapper.writeValueAsString(broadcast)
        val recordId =
            redisTemplate
                .opsForStream<String, String>()
                .add(
                    RealtimeBroadcastStreamContract.STREAM_KEY,
                    mapOf(RealtimeBroadcastStreamContract.EVENT_FIELD to serialized),
                    RedisStreamCommands.XAddOptions.trim(trimOptions),
                )

        checkNotNull(recordId) { "Redis did not acknowledge the realtime broadcast stream append" }
    }
}
