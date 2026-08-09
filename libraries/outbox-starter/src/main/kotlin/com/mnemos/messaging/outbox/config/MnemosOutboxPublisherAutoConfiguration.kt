package com.mnemos.messaging.outbox.config

import com.mnemos.messaging.core.transport.EventTransport
import com.mnemos.messaging.outbox.publisher.JdbcOutboxEventRepository
import com.mnemos.messaging.outbox.publisher.OutboxEventPublisher
import com.mnemos.messaging.outbox.publisher.OutboxEventRepository
import com.mnemos.messaging.outbox.publisher.OutboxPublisherScheduler
import com.mnemos.messaging.outbox.publisher.OutboxRetryPolicy
import com.mnemos.messaging.outbox.support.OutboxClock
import com.mnemos.messaging.outbox.support.OutboxProducer
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Bean
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcOperations
import org.springframework.scheduling.annotation.EnableScheduling
import org.springframework.transaction.PlatformTransactionManager
import org.springframework.transaction.support.TransactionTemplate
import java.util.UUID

@AutoConfiguration(after = [MnemosOutboxAutoConfiguration::class])
@ConditionalOnProperty(
    prefix = "mnemos.outbox.publisher",
    name = ["enabled"],
    havingValue = "true",
    matchIfMissing = false,
)
@EnableScheduling
@Suppress("SpringJavaInjectionPointsAutowiringInspection")
class MnemosOutboxPublisherAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(OutboxEventRepository::class)
    fun outboxEventRepository(
        jdbcOperations: NamedParameterJdbcOperations,
        transactionManager: PlatformTransactionManager,
    ): JdbcOutboxEventRepository {
        logger.info(
            "Configuring outbox event repository",
        )

        return JdbcOutboxEventRepository(
            jdbcOperations = jdbcOperations,
            transactionOperations = TransactionTemplate(transactionManager),
        )
    }

    @Bean
    @ConditionalOnMissingBean(OutboxRetryPolicy::class)
    fun outboxRetryPolicy(
        properties: MnemosOutboxProperties,
    ): OutboxRetryPolicy {
        logger.info(
            "Configuring outbox retry policy: maxAttempts={}, initialBackoff={}, maxBackoff={}",
            properties.publisher.maxAttempts,
            properties.publisher.initialBackoff,
            properties.publisher.maxBackoff,
        )

        return OutboxRetryPolicy(
            maxAttempts = properties.publisher.maxAttempts,
            initialBackoff = properties.publisher.initialBackoff,
            maxBackoff = properties.publisher.maxBackoff,
        )
    }

    @Bean
    @ConditionalOnMissingBean(OutboxEventPublisher::class)
    fun outboxEventPublisher(
        repository: OutboxEventRepository,
        eventTransport: EventTransport,
        retryPolicy: OutboxRetryPolicy,
        clock: OutboxClock,
        producer: OutboxProducer,
        properties: MnemosOutboxProperties,
    ): OutboxEventPublisher {
        val instanceId = createInstanceId(producer.value)

        logger.info(
            "Configuring outbox publisher: producer={}, instanceId={}, maxEventsPerRun={}, leaseDuration={}, sendTimeout={}",
            producer.value,
            instanceId,
            properties.publisher.maxEventsPerRun,
            properties.publisher.leaseDuration,
            properties.publisher.sendTimeout,
        )

        return OutboxEventPublisher(
            repository = repository,
            eventTransport = eventTransport,
            retryPolicy = retryPolicy,
            clock = clock,
            instanceId = instanceId,
            properties = properties.publisher,
        )
    }

    @Bean
    @ConditionalOnMissingBean(OutboxPublisherScheduler::class)
    fun outboxPublisherScheduler(
        publisher: OutboxEventPublisher,
    ): OutboxPublisherScheduler {
        logger.info(
            "Configuring outbox publisher scheduler",
        )

        return OutboxPublisherScheduler(publisher)
    }

    private fun createInstanceId(
        producer: String,
    ): String =
        "$producer:${UUID.randomUUID()}"

    private companion object {
        val logger =
            LoggerFactory.getLogger(
                MnemosOutboxPublisherAutoConfiguration::class.java,
            )
    }
}