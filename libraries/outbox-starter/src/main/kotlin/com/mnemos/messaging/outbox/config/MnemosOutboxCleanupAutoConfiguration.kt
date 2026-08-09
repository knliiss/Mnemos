package com.mnemos.messaging.outbox.config

import com.mnemos.messaging.outbox.cleanup.JdbcOutboxCleanupRepository
import com.mnemos.messaging.outbox.cleanup.OutboxCleanupRepository
import com.mnemos.messaging.outbox.cleanup.OutboxCleanupScheduler
import com.mnemos.messaging.outbox.cleanup.OutboxCleanupService
import com.mnemos.messaging.outbox.support.OutboxClock
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Bean
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcOperations
import org.springframework.scheduling.annotation.EnableScheduling

@AutoConfiguration(after = [MnemosOutboxAutoConfiguration::class])
@ConditionalOnProperty(
    prefix = "mnemos.outbox",
    name = [
        "enabled",
        "cleanup.enabled",
    ],
    havingValue = "true",
    matchIfMissing = false,
)
@EnableScheduling
class MnemosOutboxCleanupAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    fun outboxCleanupRepository(
        jdbcOperations: NamedParameterJdbcOperations,
    ): OutboxCleanupRepository =
        JdbcOutboxCleanupRepository(jdbcOperations)

    @Bean
    @ConditionalOnMissingBean
    fun outboxCleanupService(
        repository: OutboxCleanupRepository,
        clock: OutboxClock,
        properties: MnemosOutboxProperties,
    ): OutboxCleanupService =
        OutboxCleanupService(
            repository = repository,
            clock = clock,
            properties = properties.cleanup,
        )

    @Bean
    @ConditionalOnMissingBean
    fun outboxCleanupScheduler(
        cleanupService: OutboxCleanupService,
    ): OutboxCleanupScheduler =
        OutboxCleanupScheduler(cleanupService)
}