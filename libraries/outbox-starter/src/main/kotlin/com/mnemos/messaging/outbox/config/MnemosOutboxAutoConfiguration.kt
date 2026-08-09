package com.mnemos.messaging.outbox.config

import com.mnemos.messaging.core.publisher.IntegrationEventPublisher
import com.mnemos.messaging.core.publisher.IntegrationEventRouteResolver
import com.mnemos.messaging.outbox.metadata.DefaultEventMetadataProvider
import com.mnemos.messaging.outbox.metadata.EventMetadataProvider
import com.mnemos.messaging.outbox.serialization.EventEnvelopeSerializer
import com.mnemos.messaging.outbox.serialization.JacksonEventEnvelopeSerializer
import com.mnemos.messaging.outbox.support.OutboxClock
import com.mnemos.messaging.outbox.support.OutboxEventIdGenerator
import com.mnemos.messaging.outbox.support.OutboxProducer
import com.mnemos.messaging.outbox.writer.JdbcOutboxEventWriter
import com.mnemos.messaging.outbox.writer.OutboxEventWriter
import com.mnemos.messaging.outbox.writer.OutboxIntegrationEventPublisher
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.core.env.Environment
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcOperations
import tools.jackson.databind.json.JsonMapper
import java.time.Instant
import java.util.UUID

@AutoConfiguration(
    afterName = [
        "org.springframework.boot.jdbc.autoconfigure.JdbcTemplateAutoConfiguration",
        "org.springframework.boot.jackson.autoconfigure.JacksonAutoConfiguration",
    ],
)
@ConditionalOnClass(
    NamedParameterJdbcOperations::class,
    JsonMapper::class,
)
@ConditionalOnProperty(
    prefix = "mnemos.outbox",
    name = ["enabled"],
    havingValue = "true",
    matchIfMissing = false,
)
@EnableConfigurationProperties(MnemosOutboxProperties::class)
class MnemosOutboxAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    fun eventEnvelopeSerializer(
        jsonMapper: JsonMapper,
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
        DefaultEventMetadataProvider()

    @Bean
    @ConditionalOnMissingBean
    fun outboxProducer(
        properties: MnemosOutboxProperties,
        environment: Environment,
    ): OutboxProducer {
        val producer =
            properties.producer
                ?.takeIf { it.isNotBlank() }
                ?: environment
                    .getProperty("spring.application.name")
                    ?.takeIf { it.isNotBlank() }
                ?: throw IllegalStateException(
                    "Set mnemos.outbox.producer or spring.application.name",
                )

        require(PRODUCER_PATTERN.matches(producer)) {
            "Outbox producer must use lowercase kebab case: $producer"
        }

        logger.info(
            "Configuring outbox producer: producer={}",
            producer,
        )

        return OutboxProducer(producer)
    }

    @Bean
    @ConditionalOnMissingBean
    fun outboxEventWriter(
        jdbcOperations: NamedParameterJdbcOperations,
        serializer: EventEnvelopeSerializer,
        eventIdGenerator: OutboxEventIdGenerator,
        clock: OutboxClock,
        producer: OutboxProducer,
        properties: MnemosOutboxProperties,
    ): OutboxEventWriter {
        logger.info(
            "Configuring outbox event writer: producer={}, requireTransaction={}",
            producer.value,
            properties.requireTransaction,
        )

        return JdbcOutboxEventWriter(
            jdbcOperations = jdbcOperations,
            serializer = serializer,
            eventIdGenerator = eventIdGenerator,
            clock = clock,
            producer = producer.value,
            requireTransaction = properties.requireTransaction,
        )
    }

    @Bean
    @ConditionalOnMissingBean
    @Suppress("SpringJavaInjectionPointsAutowiringInspection")
    fun integrationEventPublisher(
        outboxEventWriter: OutboxEventWriter,
        routeResolver: IntegrationEventRouteResolver,
        metadataProvider: EventMetadataProvider,
        producer: OutboxProducer,
    ): IntegrationEventPublisher {
        logger.info(
            "Configuring outbox integration event publisher: producer={}",
            producer.value,
        )

        return OutboxIntegrationEventPublisher(
            outboxEventWriter = outboxEventWriter,
            routeResolver = routeResolver,
            metadataProvider = metadataProvider,
        )
    }

    private companion object {
        val PRODUCER_PATTERN =
            Regex("^[a-z][a-z0-9]*(?:-[a-z0-9]+)*$")

        val logger =
            LoggerFactory.getLogger(
                MnemosOutboxAutoConfiguration::class.java,
            )
    }
}