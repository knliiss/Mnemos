package com.mnemos.reward.adapter.out.messaging

import com.mnemos.event.contracts.EventEventContract
import com.mnemos.messaging.kafka.config.KafkaTopicDefinition
import com.mnemos.messaging.kafka.consumer.KafkaDeadLetterTopic
import com.mnemos.wallet.contracts.wallet.WalletCommandContract
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration(proxyBeanMethods = false)
class RewardKafkaTopicConfiguration {
    @Bean
    fun eventTopic(): KafkaTopicDefinition = KafkaTopicDefinition(EventEventContract.TOPIC, 3, 1)

    @Bean
    fun eventDlt(): KafkaTopicDefinition = KafkaTopicDefinition(KafkaDeadLetterTopic.name(EventEventContract.TOPIC), 3, 1)

    @Bean
    fun walletCommandTopic(): KafkaTopicDefinition = KafkaTopicDefinition(WalletCommandContract.TOPIC, 3, 1)
}
