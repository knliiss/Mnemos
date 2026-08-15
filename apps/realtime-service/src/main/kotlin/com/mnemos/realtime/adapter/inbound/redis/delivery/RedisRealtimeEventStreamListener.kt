package com.mnemos.realtime.adapter.inbound.redis.delivery

import com.mnemos.realtime.adapter.redis.delivery.protocol.RealtimeEventStreamContract
import com.mnemos.realtime.adapter.redis.delivery.serialization.RedisRealtimeEventCodec
import com.mnemos.realtime.application.delivery.port.out.ConnectedRealtimeEventSink
import com.mnemos.realtime.application.delivery.port.out.RealtimeDeliveryTelemetry
import org.slf4j.LoggerFactory
import org.springframework.data.redis.connection.stream.MapRecord
import org.springframework.data.redis.stream.StreamListener
import org.springframework.stereotype.Component

@Component
class RedisRealtimeEventStreamListener(
    private val codec: RedisRealtimeEventCodec,
    private val connectedEventSink: ConnectedRealtimeEventSink,
    private val telemetry: RealtimeDeliveryTelemetry,
) : StreamListener<String, MapRecord<String, String, String>> {
    override fun onMessage(message: MapRecord<String, String, String>) {
        val serialized = message.value[RealtimeEventStreamContract.EVENT_FIELD]

        if (serialized == null) {
            log.error(
                "Realtime stream record is missing its event field, recordId={}",
                message.id,
            )
            return
        }

        try {
            val event = codec.decode(serialized)

            telemetry.recordStreamDelivery(event)
            connectedEventSink.send(event)
        } catch (exception: Exception) {
            log.error(
                "Failed to process realtime stream record, recordId={}",
                message.id,
                exception,
            )
        }
    }

    private companion object {
        val log = LoggerFactory.getLogger(RedisRealtimeEventStreamListener::class.java)
    }
}
