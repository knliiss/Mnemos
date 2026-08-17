package com.mnemos.realtime.adapter.inbound.redis.broadcast

import com.mnemos.realtime.adapter.redis.broadcast.protocol.RealtimeBroadcastStreamContract
import com.mnemos.realtime.application.broadcast.model.RealtimeBroadcast
import com.mnemos.realtime.application.broadcast.port.out.ConnectedRealtimeBroadcastSink
import org.slf4j.LoggerFactory
import org.springframework.data.redis.connection.stream.MapRecord
import org.springframework.data.redis.stream.StreamListener
import org.springframework.stereotype.Component
import tools.jackson.databind.ObjectMapper

@Component
class RedisRealtimeBroadcastStreamListener(
    private val objectMapper: ObjectMapper,
    private val connectedBroadcastSink: ConnectedRealtimeBroadcastSink,
) : StreamListener<String, MapRecord<String, String, String>> {
    override fun onMessage(message: MapRecord<String, String, String>) {
        val serialized = message.value[RealtimeBroadcastStreamContract.EVENT_FIELD]
        if (serialized == null) {
            log.error("Realtime broadcast stream record is missing event field, recordId={}", message.id)
            return
        }

        try {
            connectedBroadcastSink.send(objectMapper.readValue(serialized, RealtimeBroadcast::class.java))
        } catch (exception: Exception) {
            log.error("Failed to process realtime broadcast stream record, recordId={}", message.id, exception)
        }
    }

    private companion object {
        val log = LoggerFactory.getLogger(RedisRealtimeBroadcastStreamListener::class.java)
    }
}
