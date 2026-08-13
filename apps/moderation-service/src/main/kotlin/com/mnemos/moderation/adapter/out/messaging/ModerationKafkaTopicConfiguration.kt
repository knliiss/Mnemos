package com.mnemos.moderation.adapter.out.messaging

import com.mnemos.messaging.kafka.config.KafkaTopicDefinition
import com.mnemos.moderation.contracts.ModerationEventContract
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration(proxyBeanMethods = false)
class ModerationKafkaTopicConfiguration {
    @Bean
    fun moderationEventsTopic(): KafkaTopicDefinition =
        KafkaTopicDefinition(
            name = ModerationEventContract.TOPIC,
            partitions = 3,
            replicationFactor = 1,
        )
}
