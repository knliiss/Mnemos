package com.mnemos.social.adapter.out.messaging

import com.mnemos.messaging.kafka.config.KafkaTopicDefinition
import com.mnemos.messaging.kafka.consumer.KafkaDeadLetterTopic
import com.mnemos.social.contracts.SocialEventContract
import com.mnemos.wallet.contracts.wallet.WalletCommandContract
import com.mnemos.wallet.contracts.wallet.WalletEventContract
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration(proxyBeanMethods = false)
class SocialKafkaTopicConfiguration {
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
