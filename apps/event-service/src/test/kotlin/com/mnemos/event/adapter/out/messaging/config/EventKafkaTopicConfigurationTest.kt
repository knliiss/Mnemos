package com.mnemos.event.adapter.out.messaging.config

import com.mnemos.realtime.contracts.collector.CollectorIngressTopicPolicy
import org.apache.kafka.common.config.TopicConfig
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class EventKafkaTopicConfigurationTest {
    private val configuration = EventKafkaTopicConfiguration()

    @Test
    fun `declares collector ingress with the shared single-partition policy`() {
        val topic = configuration.collectorIngressTopic()

        assertEquals(CollectorIngressTopicPolicy.PARTITIONS, topic.partitions)
        assertEquals(
            CollectorIngressTopicPolicy.RETENTION_BYTES,
            topic.configs[TopicConfig.RETENTION_BYTES_CONFIG],
        )
        assertEquals(
            CollectorIngressTopicPolicy.SEGMENT_BYTES,
            topic.configs[TopicConfig.SEGMENT_BYTES_CONFIG],
        )
    }

    @Test
    fun `declares collector dead letters with the shared disk cap`() {
        val topic = configuration.collectorIngressDeadLetterTopic()

        assertEquals(CollectorIngressTopicPolicy.PARTITIONS, topic.partitions)
        assertEquals(
            CollectorIngressTopicPolicy.DEAD_LETTER_RETENTION_BYTES,
            topic.configs[TopicConfig.RETENTION_BYTES_CONFIG],
        )
    }
}
