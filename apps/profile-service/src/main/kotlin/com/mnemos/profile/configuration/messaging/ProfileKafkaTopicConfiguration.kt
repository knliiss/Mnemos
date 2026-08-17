package com.mnemos.profile.configuration.messaging

import com.mnemos.contracts.account.AccountEventContract
import com.mnemos.messaging.kafka.config.KafkaTopicDefinition
import com.mnemos.messaging.kafka.consumer.KafkaDeadLetterTopic
import com.mnemos.personalization.contracts.PersonalizationEventContract
import com.mnemos.social.contracts.SocialEventContract
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration(proxyBeanMethods = false)
class ProfileKafkaTopicConfiguration {
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
    fun socialEventsTopic(): KafkaTopicDefinition =
        KafkaTopicDefinition(
            name = SocialEventContract.TOPIC,
            partitions = 3,
            replicationFactor = 1,
        )

    @Bean
    fun socialEventsDeadLetterTopic(): KafkaTopicDefinition =
        KafkaTopicDefinition(
            name = KafkaDeadLetterTopic.name(SocialEventContract.TOPIC),
            partitions = 3,
            replicationFactor = 1,
        )

    @Bean
    fun personalizationEventsTopic(): KafkaTopicDefinition =
        KafkaTopicDefinition(
            name = PersonalizationEventContract.TOPIC,
            partitions = 3,
            replicationFactor = 1,
        )

    @Bean
    fun personalizationEventsDeadLetterTopic(): KafkaTopicDefinition =
        KafkaTopicDefinition(
            name = KafkaDeadLetterTopic.name(PersonalizationEventContract.TOPIC),
            partitions = 3,
            replicationFactor = 1,
        )
}
