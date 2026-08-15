package com.mnemos.personalization.adapter.out.messaging

import com.mnemos.messaging.kafka.config.KafkaTopicDefinition
import com.mnemos.messaging.kafka.consumer.KafkaDeadLetterTopic
import com.mnemos.personalization.contracts.PersonalizationEventContract
import com.mnemos.wallet.contracts.wallet.WalletCommandContract
import com.mnemos.wallet.contracts.wallet.WalletEventContract
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration(proxyBeanMethods = false)
class PersonalizationKafkaTopicConfiguration {
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

    @Bean
    fun walletCommandsTopic(): KafkaTopicDefinition =
        KafkaTopicDefinition(
            name = WalletCommandContract.TOPIC,
            partitions = 3,
            replicationFactor = 1,
        )

    @Bean
    fun walletEventsTopic(): KafkaTopicDefinition =
        KafkaTopicDefinition(
            name = WalletEventContract.TOPIC,
            partitions = 3,
            replicationFactor = 1,
        )

    @Bean
    fun walletEventsDeadLetterTopic(): KafkaTopicDefinition =
        KafkaTopicDefinition(
            name = KafkaDeadLetterTopic.name(WalletEventContract.TOPIC),
            partitions = 3,
            replicationFactor = 1,
        )
}
