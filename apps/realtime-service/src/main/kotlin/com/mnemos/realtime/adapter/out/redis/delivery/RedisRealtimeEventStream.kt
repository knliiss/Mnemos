package com.mnemos.realtime.adapter.out.redis.delivery

import com.mnemos.realtime.adapter.redis.delivery.protocol.RealtimeEventStreamContract
import com.mnemos.realtime.adapter.redis.delivery.serialization.RedisRealtimeEventCodec
import com.mnemos.realtime.application.delivery.model.RealtimeEvent
import com.mnemos.realtime.application.delivery.port.out.ConnectedRealtimeEventSink
import com.mnemos.realtime.application.delivery.port.out.RealtimeEventFanout
import com.mnemos.realtime.configuration.delivery.RealtimeDeliveryProperties
import org.springframework.data.redis.connection.RedisStreamCommands
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.stereotype.Component

@Component
class RedisRealtimeEventStream(
    private val redisTemplate: StringRedisTemplate,
    private val codec: RedisRealtimeEventCodec,
    private val properties: RealtimeDeliveryProperties,
    private val connectedEventSink: ConnectedRealtimeEventSink,
) : RealtimeEventFanout {
    override fun publish(event: RealtimeEvent) {
        connectedEventSink.send(event)

        val trimOptions =
            RedisStreamCommands.TrimOptions
                .maxLen(properties.streamMaxLength)
                .approximate()

        val recordId =
            redisTemplate
                .opsForStream<String, String>()
                .add(
                    RealtimeEventStreamContract.STREAM_KEY,
                    mapOf(RealtimeEventStreamContract.EVENT_FIELD to codec.encode(event)),
                    RedisStreamCommands.XAddOptions.trim(trimOptions),
                )

        checkNotNull(recordId) {
            "Redis did not acknowledge the realtime stream append"
        }
    }
}
