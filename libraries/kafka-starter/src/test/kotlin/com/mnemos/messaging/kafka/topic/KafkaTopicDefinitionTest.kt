package com.mnemos.messaging.kafka.topic

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test

class KafkaTopicDefinitionTest {

    @Test
    fun `convert definition to kafka topic`() {
        val definition = KafkaTopicDefinition(
            name = "mnemos.account.events.v1",
            partitions = 3,
            replicationFactor = 1,
            configs = mapOf(
                "retention.ms" to "604800000"
            )
        )

        val topic = definition.toNewTopic()

        assertEquals(
            "mnemos.account.events.v1",
            topic.name()
        )

        assertEquals(
            3,
            topic.numPartitions()
        )

        assertEquals(
            1,
            topic.replicationFactor().toInt()
        )

        assertEquals(
            "604800000",
            topic.configs()["retention.ms"]
        )
    }

    @Test
    fun `topic partitions must be positive`() {
        assertThrows(IllegalArgumentException::class.java) {
            KafkaTopicDefinition(
                name = "mnemos.account.events.v1",
                partitions = 0,
                replicationFactor = 1
            )
        }
    }

    @Test
    fun `topic replication factor must be positive`() {
        assertThrows(IllegalArgumentException::class.java) {
            KafkaTopicDefinition(
                name = "mnemos.account.events.v1",
                partitions = 3,
                replicationFactor = 0
            )
        }
    }

    @Test
    fun `topic name must contain supported characters`() {
        assertThrows(IllegalArgumentException::class.java) {
            KafkaTopicDefinition(
                name = "mnemos account events",
                partitions = 3,
                replicationFactor = 1
            )
        }
    }
}