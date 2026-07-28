package com.mnemos.messaging.outbox.config

import com.mnemos.messaging.core.transport.EventTransport
import com.mnemos.messaging.outbox.publisher.JdbcOutboxEventRepository
import com.mnemos.messaging.outbox.publisher.OutboxEventPublisher
import com.mnemos.messaging.outbox.publisher.OutboxEventRepository
import com.mnemos.messaging.outbox.publisher.OutboxPublisherScheduler
import com.mnemos.messaging.outbox.publisher.OutboxRetryPolicy
import com.mnemos.messaging.outbox.support.OutboxClock
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Bean
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcOperations
import org.springframework.scheduling.annotation.EnableScheduling
import org.springframework.transaction.PlatformTransactionManager
import org.springframework.transaction.support.TransactionTemplate
import java.util.UUID

@AutoConfiguration(after = [MnemosOutboxAutoConfiguration::class])
@ConditionalOnBean(
    EventTransport::class,
    NamedParameterJdbcOperations::class,
    PlatformTransactionManager::class
)
@ConditionalOnProperty(
    prefix = "mnemos.outbox.publisher",
    name = ["enabled"],
    havingValue = "true",
    matchIfMissing = false
)
@EnableScheduling
class MnemosOutboxPublisherAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    fun outboxEventRepository(
        jdbcOperations: NamedParameterJdbcOperations,
        transactionManager: PlatformTransactionManager
    ): OutboxEventRepository =
        JdbcOutboxEventRepository(
            jdbcOperations = jdbcOperations,
            transactionOperations =
                TransactionTemplate(transactionManager)
        )

    @Bean
    @ConditionalOnMissingBean
    fun outboxRetryPolicy(
        properties: MnemosOutboxProperties
    ): OutboxRetryPolicy =
        OutboxRetryPolicy(
            maxAttempts =
                properties.publisher.maxAttempts,
            initialBackoff =
                properties.publisher.initialBackoff,
            maxBackoff =
                properties.publisher.maxBackoff
        )

    @Bean
    @ConditionalOnMissingBean
    fun outboxEventPublisher(
        repository: OutboxEventRepository,
        eventTransport: EventTransport,
        retryPolicy: OutboxRetryPolicy,
        clock: OutboxClock,
        properties: MnemosOutboxProperties
    ): OutboxEventPublisher =
        OutboxEventPublisher(
            repository = repository,
            eventTransport = eventTransport,
            retryPolicy = retryPolicy,
            clock = clock,
            instanceId = createInstanceId(
                producer = requireNotNull(
                    properties.producer
                ) {
                    "mnemos.outbox.producer must be configured"
                }
            ),
            properties = properties.publisher
        )

    @Bean
    @ConditionalOnMissingBean
    fun outboxPublisherScheduler(
        publisher: OutboxEventPublisher
    ): OutboxPublisherScheduler =
        OutboxPublisherScheduler(publisher)

    private fun createInstanceId(producer: String): String =
        "$producer:${UUID.randomUUID()}"
}