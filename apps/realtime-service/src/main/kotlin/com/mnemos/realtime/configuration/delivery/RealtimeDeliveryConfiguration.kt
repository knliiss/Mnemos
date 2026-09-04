package com.mnemos.realtime.configuration.delivery

import com.mnemos.realtime.adapter.inbound.redis.delivery.RedisRealtimeEventStreamListener
import com.mnemos.realtime.adapter.redis.delivery.protocol.RealtimeEventStreamContract
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.redis.connection.RedisConnectionFactory
import org.springframework.data.redis.connection.stream.MapRecord
import org.springframework.data.redis.connection.stream.ReadOffset
import org.springframework.data.redis.connection.stream.StreamOffset
import org.springframework.data.redis.stream.StreamMessageListenerContainer

@Configuration(proxyBeanMethods = false)
class RealtimeDeliveryConfiguration {
    @Bean(initMethod = "start", destroyMethod = "stop")
    fun realtimeEventStreamListenerContainer(
        connectionFactory: RedisConnectionFactory,
        listener: RedisRealtimeEventStreamListener,
        properties: RealtimeDeliveryProperties,
    ): StreamMessageListenerContainer<String, MapRecord<String, String, String>> {
        val options =
            StreamMessageListenerContainer.StreamMessageListenerContainerOptions
                .builder()
                .pollTimeout(properties.streamPollTimeout)
                .batchSize(properties.streamBatchSize)
                .build()

        val container =
            StreamMessageListenerContainer.create(
                connectionFactory,
                options,
            )

        val streamOffset =
            StreamOffset.create(
                RealtimeEventStreamContract.STREAM_KEY,
                ReadOffset.lastConsumed(),
            )
        val request =
            StreamMessageListenerContainer.StreamReadRequest
                .builder(streamOffset)
                .errorHandler { exception ->
                    log.error("Realtime stream polling failed", exception)
                }.cancelOnError { false }
                .build()

        container.register(request, listener)

        return container
    }

    private companion object {
        val log = LoggerFactory.getLogger(RealtimeDeliveryConfiguration::class.java)
    }
}
