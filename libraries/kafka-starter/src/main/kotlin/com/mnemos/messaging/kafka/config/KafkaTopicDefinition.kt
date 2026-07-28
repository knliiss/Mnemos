package com.mnemos.messaging.kafka.topic

import org.apache.kafka.clients.admin.NewTopic
import org.springframework.kafka.config.TopicBuilder

data class KafkaTopicDefinition(
    val name: String,
    val partitions: Int,
    val replicationFactor: Short,
    val configs: Map<String, String> = emptyMap()
) {
    init {
        require(TOPIC_PATTERN.matches(name)) {
            "Kafka topic contains unsupported characters: $name"
        }

        require(partitions > 0) {
            "Kafka topic partitions must be positive"
        }

        require(replicationFactor > 0) {
            "Kafka topic replication factor must be positive"
        }
    }

    fun toNewTopic(): NewTopic {
        val builder = TopicBuilder
            .name(name)
            .partitions(partitions)
            .replicas(replicationFactor.toInt())

        configs.forEach { (name, value) ->
            builder.config(name, value)
        }

        return builder.build()
    }

    private companion object {
        val TOPIC_PATTERN = Regex("^[a-zA-Z0-9._-]+$")
    }
}