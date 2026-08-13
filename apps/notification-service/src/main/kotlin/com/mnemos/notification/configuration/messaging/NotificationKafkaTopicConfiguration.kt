package com.mnemos.notification.configuration.messaging

import com.mnemos.contracts.account.AccountEventContract
import com.mnemos.event.contracts.EventEventContract
import com.mnemos.messaging.kafka.config.KafkaTopicDefinition
import com.mnemos.messaging.kafka.consumer.KafkaDeadLetterTopic
import com.mnemos.moderation.contracts.ModerationEventContract
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration(proxyBeanMethods = false)
class NotificationKafkaTopicConfiguration {
    @Bean
    fun accountEventsTopic(): KafkaTopicDefinition =
        KafkaTopicDefinition(
            name = AccountEventContract.TOPIC,
            partitions = 3,
            replicationFactor = 1,
        )

    @Bean
    fun accountEventsDeadLetterTopic(): KafkaTopicDefinition =
        KafkaTopicDefinition(
            name = KafkaDeadLetterTopic.name(AccountEventContract.TOPIC),
            partitions = 3,
            replicationFactor = 1,
        )

    @Bean
    fun eventEventsTopic(): KafkaTopicDefinition =
        KafkaTopicDefinition(
            name = EventEventContract.TOPIC,
            partitions = 3,
            replicationFactor = 1,
        )

    @Bean
    fun eventEventsDeadLetterTopic(): KafkaTopicDefinition =
        KafkaTopicDefinition(
            name = KafkaDeadLetterTopic.name(EventEventContract.TOPIC),
            partitions = 3,
            replicationFactor = 1,
        )

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
