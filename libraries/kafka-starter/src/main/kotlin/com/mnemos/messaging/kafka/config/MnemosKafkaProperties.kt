package com.mnemos.messaging.kafka.config

import org.springframework.boot.context.properties.ConfigurationProperties
import java.time.Duration

@ConfigurationProperties("mnemos.kafka")
data class MnemosKafkaProperties(
    val consumer: Consumer = Consumer(),
    val messageAuth: MessageAuth = MessageAuth(),
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

    data class MessageAuth(
        val enabled: Boolean = true,
        val privateKey: String = "",
        val trustedPublicKeys: String = "",
    ) {
        fun requireTrustedPublicKeys(): Map<String, String> {
            require(trustedPublicKeys.isNotBlank()) {
                "Kafka message authentication trusted public keys must not be blank"
            }

            val entries =
                trustedPublicKeys
                    .split(';')
                    .map(String::trim)
                    .filter(String::isNotEmpty)

            val parsed =
                entries.map { entry ->
                    val separator = entry.indexOf('=')

                    require(separator > 0 && separator < entry.lastIndex) {
                        "Kafka trusted public key must use producer=base64 format"
                    }

                    val producer = entry.substring(0, separator).trim()
                    val publicKey = entry.substring(separator + 1).trim()

                    require(PRODUCER_PATTERN.matches(producer)) {
                        "Invalid Kafka trusted producer: $producer"
                    }
                    require(publicKey.isNotBlank()) {
                        "Kafka trusted public key for $producer must not be blank"
                    }

                    producer to publicKey
                }

            require(parsed.map { it.first }.distinct().size == parsed.size) {
                "Kafka trusted public key producers must be unique"
            }

            return parsed.toMap()
        }

        private companion object {
            val PRODUCER_PATTERN = Regex("^[a-z][a-z0-9]*(?:-[a-z0-9]+)*$")
        }
    }
}
