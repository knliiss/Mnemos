package com.mnemos.gateway.messaging

import com.mnemos.messaging.kafka.config.KafkaTopicDefinition
import com.mnemos.messaging.kafka.consumer.KafkaDeadLetterTopic
import com.mnemos.moderation.contracts.ModerationEventContract
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration(proxyBeanMethods = false)
class GatewayKafkaTopicConfiguration {
    @Bean
    fun moderationEventsTopic(): KafkaTopicDefinition =
        KafkaTopicDefinition(
            name = ModerationEventContract.TOPIC,
            partitions = 3,
            replicationFactor = 1,
        )

    @Bean
    fun moderationEventsDeadLetterTopic(): KafkaTopicDefinition =
        KafkaTopicDefinition(
            name = KafkaDeadLetterTopic.name(ModerationEventContract.TOPIC),
            partitions = 3,
            replicationFactor = 1,
        )
}
