package com.mnemos.wallet.infrastructure.messaging.kafka.config

import com.mnemos.contracts.account.AccountEventContract
import com.mnemos.messaging.kafka.config.KafkaTopicDefinition
import com.mnemos.messaging.kafka.consumer.KafkaDeadLetterTopic
import com.mnemos.wallet.contracts.wallet.WalletCommandContract
import com.mnemos.wallet.contracts.wallet.WalletEventContract
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@ConditionalOnProperty(
    prefix = "mnemos.kafka",
    name = ["enabled"],
    havingValue = "true",
    matchIfMissing = true,
)
@Configuration(proxyBeanMethods = false)
class WalletKafkaTopicConfiguration {
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
    fun walletCommandsTopic(): KafkaTopicDefinition =
        KafkaTopicDefinition(
            name = WalletCommandContract.TOPIC,
            partitions = 3,
            replicationFactor = 1,
        )

    @Bean
    fun walletCommandsDeadLetterTopic(): KafkaTopicDefinition =
        KafkaTopicDefinition(
            name = KafkaDeadLetterTopic.name(WalletCommandContract.TOPIC),
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
}
