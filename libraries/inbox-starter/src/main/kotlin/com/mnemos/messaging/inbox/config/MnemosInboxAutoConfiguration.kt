package com.mnemos.messaging.inbox.config

import com.mnemos.messaging.inbox.InboxClock
import com.mnemos.messaging.inbox.InboxEventProcessor
import com.mnemos.messaging.inbox.InboxEventRepository
import com.mnemos.messaging.inbox.JdbcInboxEventRepository
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcOperations
import org.springframework.transaction.PlatformTransactionManager
import org.springframework.transaction.support.TransactionTemplate
import java.time.Instant

@AutoConfiguration(
    afterName = [
        "org.springframework.boot.jdbc.autoconfigure.JdbcTemplateAutoConfiguration",
        "org.springframework.boot.hibernate.autoconfigure.HibernateJpaAutoConfiguration",
    ],
)
@ConditionalOnProperty(
    prefix = "mnemos.inbox",
    name = ["enabled"],
    havingValue = "true",
    matchIfMissing = false,
)
@EnableConfigurationProperties(
    MnemosInboxProperties::class,
)
class MnemosInboxAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    fun inboxClock(): InboxClock =
        InboxClock(Instant::now)

    @Bean
    @ConditionalOnMissingBean
    fun inboxEventRepository(
        jdbcOperations: NamedParameterJdbcOperations,
    ): InboxEventRepository =
        JdbcInboxEventRepository(
            jdbcOperations,
        )

    @Bean
    @ConditionalOnMissingBean
    fun inboxEventProcessor(
        repository: InboxEventRepository,
        transactionManager: PlatformTransactionManager,
        clock: InboxClock,
    ): InboxEventProcessor =
        InboxEventProcessor(
            repository = repository,
            transactionOperations =
                TransactionTemplate(
                    transactionManager,
                ),
            clock = clock,
        )
}