package com.mnemos.messaging.inbox.config

import com.mnemos.messaging.inbox.InboxClock
import com.mnemos.messaging.inbox.cleanup.InboxCleanupRepository
import com.mnemos.messaging.inbox.cleanup.InboxCleanupScheduler
import com.mnemos.messaging.inbox.cleanup.InboxCleanupService
import com.mnemos.messaging.inbox.cleanup.JdbcInboxCleanupRepository
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Bean
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcOperations
import org.springframework.scheduling.annotation.EnableScheduling

@AutoConfiguration(after = [MnemosInboxAutoConfiguration::class])
@ConditionalOnProperty(
    prefix = "mnemos.inbox",
    name = ["enabled"],
    havingValue = "true",
    matchIfMissing = false,
)
@EnableScheduling
class MnemosInboxCleanupAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    fun inboxCleanupRepository(
        jdbcOperations: NamedParameterJdbcOperations,
    ): InboxCleanupRepository = JdbcInboxCleanupRepository(jdbcOperations)

    @Bean
    @ConditionalOnMissingBean
    fun inboxCleanupService(
        repository: InboxCleanupRepository,
        clock: InboxClock,
        properties: MnemosInboxProperties,
    ): InboxCleanupService = InboxCleanupService(
        repository = repository,
        clock = clock,
        properties = properties.cleanup,
    )

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(
        prefix = "mnemos.inbox.cleanup",
        name = ["enabled"],
        havingValue = "true",
        matchIfMissing = true,
    )
    fun inboxCleanupScheduler(
        cleanupService: InboxCleanupService,
    ): InboxCleanupScheduler = InboxCleanupScheduler(cleanupService)
}