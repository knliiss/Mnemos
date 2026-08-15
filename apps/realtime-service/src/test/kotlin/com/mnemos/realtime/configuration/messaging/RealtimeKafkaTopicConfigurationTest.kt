package com.mnemos.realtime.configuration.messaging

import com.mnemos.realtime.contracts.collector.CollectorIngressTopicPolicy
import org.apache.kafka.common.config.TopicConfig
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class RealtimeKafkaTopicConfigurationTest {
    private val configuration = RealtimeKafkaTopicConfiguration()

    @Test
    fun `collector ingress is single partition and bounded to 256 MiB`() {
        val topic = configuration.collectorIngressTopic()

        assertEquals(CollectorIngressTopicPolicy.PARTITIONS, topic.partitions)
        assertEquals(
            CollectorIngressTopicPolicy.RETENTION_BYTES,
            topic.configs[TopicConfig.RETENTION_BYTES_CONFIG],
        )
        assertEquals("lz4", topic.configs[TopicConfig.COMPRESSION_TYPE_CONFIG])
    }

    @Test
    fun `collector dead letter storage is bounded to 64 MiB`() {
        val topic = configuration.collectorIngressDeadLetterTopic()

        assertEquals(CollectorIngressTopicPolicy.PARTITIONS, topic.partitions)
        assertEquals(
            CollectorIngressTopicPolicy.DEAD_LETTER_RETENTION_BYTES,
            topic.configs[TopicConfig.RETENTION_BYTES_CONFIG],
        )
    }
}
