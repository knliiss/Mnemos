package com.mnemos.bet.adapter.out.messaging

import com.mnemos.bet.contracts.BetEventContract
import com.mnemos.event.contracts.EventEventContract
import com.mnemos.messaging.kafka.config.KafkaTopicDefinition
import com.mnemos.messaging.kafka.consumer.KafkaDeadLetterTopic
import com.mnemos.wallet.contracts.wallet.WalletCommandContract
import com.mnemos.wallet.contracts.wallet.WalletEventContract
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration(proxyBeanMethods = false)
class BetKafkaTopicConfiguration {
    @Bean
    fun betEventsTopic(): KafkaTopicDefinition =
        KafkaTopicDefinition(
            name = BetEventContract.TOPIC,
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
