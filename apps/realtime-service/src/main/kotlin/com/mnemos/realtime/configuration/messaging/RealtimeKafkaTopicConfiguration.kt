package com.mnemos.realtime.configuration.messaging

import com.mnemos.messaging.kafka.config.KafkaTopicDefinition
import com.mnemos.messaging.kafka.consumer.KafkaDeadLetterTopic
import com.mnemos.realtime.contracts.collector.CollectorIngressContract
import com.mnemos.realtime.contracts.collector.CollectorIngressTopicPolicy
import org.apache.kafka.common.config.TopicConfig
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration(proxyBeanMethods = false)
class RealtimeKafkaTopicConfiguration {
    @Bean
    fun collectorIngressTopic(): KafkaTopicDefinition =
        KafkaTopicDefinition(
            name = CollectorIngressContract.TOPIC,
            partitions = CollectorIngressTopicPolicy.PARTITIONS,
            replicationFactor = 1,
            configs =
                mapOf(
                    TopicConfig.CLEANUP_POLICY_CONFIG to TopicConfig.CLEANUP_POLICY_DELETE,
                    TopicConfig.RETENTION_MS_CONFIG to CollectorIngressTopicPolicy.RETENTION_MS,
                    TopicConfig.RETENTION_BYTES_CONFIG to CollectorIngressTopicPolicy.RETENTION_BYTES,
                    TopicConfig.SEGMENT_MS_CONFIG to CollectorIngressTopicPolicy.SEGMENT_MS,
                    TopicConfig.SEGMENT_BYTES_CONFIG to CollectorIngressTopicPolicy.SEGMENT_BYTES,
                    TopicConfig.COMPRESSION_TYPE_CONFIG to "lz4",
                ),
        )

    @Bean
    fun collectorIngressDeadLetterTopic(): KafkaTopicDefinition =
        KafkaTopicDefinition(
            name = KafkaDeadLetterTopic.name(CollectorIngressContract.TOPIC),
            partitions = CollectorIngressTopicPolicy.PARTITIONS,
            replicationFactor = 1,
            configs =
                mapOf(
                    TopicConfig.CLEANUP_POLICY_CONFIG to TopicConfig.CLEANUP_POLICY_DELETE,
                    TopicConfig.RETENTION_MS_CONFIG to CollectorIngressTopicPolicy.DEAD_LETTER_RETENTION_MS,
                    TopicConfig.RETENTION_BYTES_CONFIG to CollectorIngressTopicPolicy.DEAD_LETTER_RETENTION_BYTES,
                ),
        )
}
