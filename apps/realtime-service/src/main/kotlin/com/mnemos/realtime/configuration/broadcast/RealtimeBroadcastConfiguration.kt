package com.mnemos.realtime.configuration.broadcast

import com.mnemos.realtime.adapter.inbound.redis.broadcast.RedisRealtimeBroadcastStreamListener
import com.mnemos.realtime.adapter.redis.broadcast.protocol.RealtimeBroadcastStreamContract
import com.mnemos.realtime.configuration.delivery.RealtimeDeliveryProperties
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.redis.connection.RedisConnectionFactory
import org.springframework.data.redis.connection.stream.MapRecord
import org.springframework.data.redis.connection.stream.ReadOffset
import org.springframework.data.redis.connection.stream.StreamOffset
import org.springframework.data.redis.stream.StreamMessageListenerContainer

@Configuration(proxyBeanMethods = false)
class RealtimeBroadcastConfiguration {
    @Bean
    fun realtimeBroadcastStreamListenerContainer(
        connectionFactory: RedisConnectionFactory,
        listener: RedisRealtimeBroadcastStreamListener,
        properties: RealtimeDeliveryProperties,
    ): StreamMessageListenerContainer<String, MapRecord<String, String, String>> {
        val options =
            StreamMessageListenerContainer.StreamMessageListenerContainerOptions
                .builder()
                .pollTimeout(properties.streamPollTimeout)
                .batchSize(properties.streamBatchSize)
                .build()
        val container = StreamMessageListenerContainer.create(connectionFactory, options)
        val streamOffset = StreamOffset.create(RealtimeBroadcastStreamContract.STREAM_KEY, ReadOffset.lastConsumed())
        val request =
            StreamMessageListenerContainer.StreamReadRequest
                .builder(streamOffset)
                .errorHandler { exception -> log.error("Realtime broadcast stream polling failed", exception) }
                .cancelOnError { false }
                .build()

        container.register(request, listener)
        return container
    }

    private companion object {
        val log = LoggerFactory.getLogger(RealtimeBroadcastConfiguration::class.java)
    }
}
