package com.mnemos.account.infrastructure.messaging.kafka.config

import com.mnemos.contracts.account.AccountEventContract
import com.mnemos.messaging.kafka.config.KafkaTopicDefinition
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@ConditionalOnProperty(
    prefix = "mnemos.kafka",
    name = ["enabled"],
    havingValue = "true",
    matchIfMissing = true,
)
@Configuration
class AccountKafkaTopicConfiguration {
    @Bean
    fun accountEventsTopic(): KafkaTopicDefinition =
        KafkaTopicDefinition(
            name = AccountEventContract.TOPIC,
            partitions = 3,
            replicationFactor = 1,
        )
}
