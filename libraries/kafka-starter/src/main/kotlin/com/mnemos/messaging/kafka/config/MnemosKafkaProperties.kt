package com.mnemos.messaging.kafka.config

import org.springframework.boot.context.properties.ConfigurationProperties
import java.time.Duration

@ConfigurationProperties("mnemos.kafka")
data class MnemosKafkaProperties(
    val consumer: Consumer = Consumer(),
) {
    data class Consumer(
        val retry: Retry = Retry(),
    )

    data class Retry(
        val maxAttempts: Int = 4,
        val initialBackoff: Duration = Duration.ofSeconds(1),
        val multiplier: Double = 2.0,
        val maxBackoff: Duration = Duration.ofSeconds(30),
    ) {
        init {
            require(maxAttempts > 0) {
                "Kafka consumer retry maxAttempts must be positive"
            }
            require(!initialBackoff.isZero && !initialBackoff.isNegative) {
                "Kafka consumer retry initialBackoff must be positive"
            }
            require(multiplier >= 1.0) {
                "Kafka consumer retry multiplier must be at least 1.0"
            }
            require(maxBackoff >= initialBackoff) {
                "Kafka consumer retry maxBackoff must be greater than or equal to initialBackoff"
            }
        }
    }
}