package com.mnemos.messaging.outbox.config

import com.mnemos.messaging.outbox.metadata.EventMetadataProvider
import com.mnemos.messaging.outbox.metadata.MdcEventMetadataProvider
import com.mnemos.messaging.outbox.serialization.EventEnvelopeSerializer
import com.mnemos.messaging.outbox.serialization.JacksonEventEnvelopeSerializer
import com.mnemos.messaging.outbox.support.OutboxClock
import com.mnemos.messaging.outbox.support.OutboxEventIdGenerator
import com.mnemos.messaging.outbox.writer.JdbcOutboxEventWriter
import com.mnemos.messaging.outbox.writer.OutboxEventWriter
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.core.env.Environment
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcOperations
import tools.jackson.databind.json.JsonMapper
import java.time.Instant
import java.util.*

@AutoConfiguration(
    afterName = [
        "org.springframework.boot.jdbc.autoconfigure.JdbcTemplateAutoConfiguration",
        "org.springframework.boot.jackson.autoconfigure.JacksonAutoConfiguration"
    ]
)
@ConditionalOnClass(
    NamedParameterJdbcOperations::class,
    JsonMapper::class
)
@ConditionalOnBean(
    NamedParameterJdbcOperations::class,
    JsonMapper::class
)
@ConditionalOnProperty(
    prefix = "mnemos.outbox",
    name = ["enabled"],
    havingValue = "true",
    matchIfMissing = true
)
@EnableConfigurationProperties(MnemosOutboxProperties::class)
class MnemosOutboxAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    fun eventEnvelopeSerializer(
        jsonMapper: JsonMapper
    ): EventEnvelopeSerializer =
        JacksonEventEnvelopeSerializer(jsonMapper)

    @Bean
    @ConditionalOnMissingBean
    fun outboxEventIdGenerator(): OutboxEventIdGenerator =
        OutboxEventIdGenerator(UUID::randomUUID)

    @Bean
    @ConditionalOnMissingBean
    fun outboxClock(): OutboxClock =
        OutboxClock(Instant::now)

    @Bean
    @ConditionalOnMissingBean
    fun eventMetadataProvider(): EventMetadataProvider =
        MdcEventMetadataProvider()

    @Bean
    @ConditionalOnMissingBean
    fun outboxEventWriter(
        jdbcOperations: NamedParameterJdbcOperations,
        serializer: EventEnvelopeSerializer,
        metadataProvider: EventMetadataProvider,
        eventIdGenerator: OutboxEventIdGenerator,
        clock: OutboxClock,
        properties: MnemosOutboxProperties,
        environment: Environment
    ): OutboxEventWriter {
        val producer = resolveProducer(
            properties = properties,
            environment = environment
        )

        return JdbcOutboxEventWriter(
            jdbcOperations = jdbcOperations,
            serializer = serializer,
            metadataProvider = metadataProvider,
            eventIdGenerator = eventIdGenerator,
            clock = clock,
            producer = producer,
            requireTransaction = properties.requireTransaction
        )
    }

    private fun resolveProducer(
        properties: MnemosOutboxProperties,
        environment: Environment
    ): String {
        val producer = properties.producer
            ?: environment.getProperty("spring.application.name")
            ?: throw IllegalStateException(
                "Set mnemos.outbox.producer or spring.application.name"
            )

        require(PRODUCER_PATTERN.matches(producer)) {
            "Outbox producer must use lowercase kebab case: $producer"
        }

        return producer
    }

    private companion object {
        val PRODUCER_PATTERN =
            Regex("^[a-z][a-z0-9]*(?:-[a-z0-9]+)*$")
    }
}