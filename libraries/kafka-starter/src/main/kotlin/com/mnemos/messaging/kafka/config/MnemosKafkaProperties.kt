package com.mnemos.messaging.kafka.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties("mnemos.kafka")
data class MnemosKafkaProperties(
    val enabled: Boolean = false,
    val manageTopics: Boolean = true
)