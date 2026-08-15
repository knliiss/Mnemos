package com.mnemos.messaging.kafka.config

import com.mnemos.messaging.core.transport.EventTransport
import com.mnemos.messaging.kafka.security.KafkaMessageAuthenticator
import com.mnemos.messaging.kafka.serialization.KafkaEventEnvelopeReader
import com.mnemos.messaging.kafka.transport.KafkaEventTransport
import org.springframework.beans.factory.ObjectProvider
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.kafka.autoconfigure.KafkaAutoConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.kafka.core.KafkaAdmin
import org.springframework.kafka.core.KafkaTemplate
import tools.jackson.databind.json.JsonMapper

@AutoConfiguration(after = [KafkaAutoConfiguration::class])
@ConditionalOnClass(KafkaTemplate::class)
@ConditionalOnProperty(
    prefix = "mnemos.kafka",
    name = ["enabled"],
    havingValue = "true",
    matchIfMissing = true,
)
@EnableConfigurationProperties(MnemosKafkaProperties::class)
class MnemosKafkaAutoConfiguration {
    @Bean
    @ConditionalOnProperty(
        prefix = "mnemos.kafka.message-auth",
        name = ["enabled"],
        havingValue = "true",
        matchIfMissing = true,
    )
    fun kafkaMessageAuthenticator(
        properties: MnemosKafkaProperties,
        @Value("\${spring.application.name}") serviceName: String,
    ): KafkaMessageAuthenticator =
        KafkaMessageAuthenticator.fromEncodedKeys(
            serviceName = serviceName,
            encodedPrivateKey = properties.messageAuth.privateKey,
            encodedTrustedPublicKeys = properties.messageAuth.requireTrustedPublicKeys(),
        )

    @Bean
    @ConditionalOnBean(KafkaTemplate::class)
    @ConditionalOnMissingBean(EventTransport::class)
    fun kafkaEventTransport(
        kafkaTemplate: KafkaTemplate<String, String>,
        messageAuthenticator: ObjectProvider<KafkaMessageAuthenticator>,
    ): EventTransport =
        KafkaEventTransport(
            kafkaTemplate = kafkaTemplate,
            messageAuthenticator = messageAuthenticator.getIfAvailable(),
        )

    @Bean
    @ConditionalOnMissingBean
    fun kafkaEventEnvelopeReader(
        jsonMapper: JsonMapper,
    ): KafkaEventEnvelopeReader = KafkaEventEnvelopeReader(jsonMapper)

    @Bean(name = ["mnemosKafkaTopics"])
    @ConditionalOnBean(KafkaTopicDefinition::class)
    @ConditionalOnMissingBean(name = ["mnemosKafkaTopics"])
    @ConditionalOnProperty(
        prefix = "mnemos.kafka",
        name = ["manage-topics"],
        havingValue = "true",
        matchIfMissing = true,
    )
    fun mnemosKafkaTopics(
        definitions: ObjectProvider<KafkaTopicDefinition>,
    ): KafkaAdmin.NewTopics {
        val topics =
            definitions
                .orderedStream()
                .map(KafkaTopicDefinition::toNewTopic)
                .toList()

        return KafkaAdmin.NewTopics(*topics.toTypedArray())
    }
}
