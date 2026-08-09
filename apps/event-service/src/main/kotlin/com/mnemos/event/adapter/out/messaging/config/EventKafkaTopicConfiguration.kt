package com.mnemos.event.adapter.out.messaging.config

import com.mnemos.event.contracts.EventEventContract
import com.mnemos.messaging.kafka.config.KafkaTopicDefinition
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration(proxyBeanMethods = false)
class EventKafkaTopicConfiguration {
    @Bean
    fun eventEventsTopic(): KafkaTopicDefinition =
        KafkaTopicDefinition(
            name = EventEventContract.TOPIC,
            partitions = 3,
            replicationFactor = 1,
        )
}
