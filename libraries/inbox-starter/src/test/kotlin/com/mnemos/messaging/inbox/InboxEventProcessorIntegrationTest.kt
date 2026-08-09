package com.mnemos.messaging.inbox

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.jdbc.datasource.DataSourceTransactionManager
import org.springframework.jdbc.datasource.DriverManagerDataSource
import org.springframework.transaction.support.TransactionTemplate
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import org.testcontainers.postgresql.PostgreSQLContainer
import java.time.Instant
import java.util.UUID
import java.util.concurrent.atomic.AtomicInteger

@Testcontainers
class InboxEventProcessorIntegrationTest {

    private lateinit var jdbc:
            NamedParameterJdbcTemplate

    private lateinit var processor:
            InboxEventProcessor

    @BeforeEach
    fun setUp() {
        val dataSource =
            DriverManagerDataSource(
                postgres.jdbcUrl,
                postgres.username,
                postgres.password,
            )

        jdbc =
            NamedParameterJdbcTemplate(
                dataSource,
            )

        jdbc.jdbcTemplate.execute(
            """
            CREATE TABLE IF NOT EXISTS inbox_events
            (
                consumer VARCHAR(150) NOT NULL,
                event_id UUID NOT NULL,
                recorded_at TIMESTAMPTZ NOT NULL,

                CONSTRAINT pk_inbox_events
                    PRIMARY KEY (
                        consumer,
                        event_id
                    ),

                CONSTRAINT ck_inbox_consumer_not_blank
                    CHECK (
                        LENGTH(TRIM(consumer)) > 0
                    )
            )
            """.trimIndent(),
        )

        jdbc.jdbcTemplate.execute(
            "TRUNCATE TABLE inbox_events",
        )

        processor =
            InboxEventProcessor(
                repository =
                    JdbcInboxEventRepository(
                        jdbc,
                    ),
                transactionOperations =
                    TransactionTemplate(
                        DataSourceTransactionManager(
                            dataSource,
                        ),
                    ),
                clock =
                    InboxClock {
                        FIXED_TIME
                    },
            )
    }

    @Test
    fun `same event and consumer is processed once`() {
        val eventId =
            UUID.randomUUID()

        val calls =
            AtomicInteger()

        val first =
            processor.process(
                consumer =
                    "wallet-service.account-created",
                eventId =
                    eventId,
            ) {
                calls.incrementAndGet()
            }

        val second =
            processor.process(
                consumer =
                    "wallet-service.account-created",
                eventId =
                    eventId,
            ) {
                calls.incrementAndGet()
            }

        assertEquals(
            InboxProcessingResult.PROCESSED,
            first,
        )

        assertEquals(
            InboxProcessingResult.DUPLICATE,
            second,
        )

        assertEquals(
            1,
            calls.get(),
        )
    }

    @Test
    fun `same event can be processed by different consumers`() {
        val eventId =
            UUID.randomUUID()

        val calls =
            AtomicInteger()

        val first =
            processor.process(
                consumer =
                    "wallet-service.account-created-balances",
                eventId =
                    eventId,
            ) {
                calls.incrementAndGet()
            }

        val second =
            processor.process(
                consumer =
                    "wallet-service.account-created-statistics",
                eventId =
                    eventId,
            ) {
                calls.incrementAndGet()
            }

        assertEquals(
            InboxProcessingResult.PROCESSED,
            first,
        )

        assertEquals(
            InboxProcessingResult.PROCESSED,
            second,
        )

        assertEquals(
            2,
            calls.get(),
        )
    }

    @Test
    fun `handler failure rolls back inbox marker`() {
        val eventId =
            UUID.randomUUID()

        assertThrows(
            IllegalStateException::class.java,
        ) {
            processor.process(
                consumer =
                    "wallet-service.account-created",
                eventId =
                    eventId,
            ) {
                throw IllegalStateException(
                    "failure",
                )
            }
        }

        assertEquals(
            0,
            countInboxRows(
                eventId,
            ),
        )

        val calls =
            AtomicInteger()

        val result =
            processor.process(
                consumer =
                    "wallet-service.account-created",
                eventId =
                    eventId,
            ) {
                calls.incrementAndGet()
            }

        assertEquals(
            InboxProcessingResult.PROCESSED,
            result,
        )

        assertEquals(
            1,
            calls.get(),
        )

        assertEquals(
            1,
            countInboxRows(
                eventId,
            ),
        )
    }

    private fun countInboxRows(
        eventId: UUID,
    ): Int =
        requireNotNull(
            jdbc.jdbcTemplate.queryForObject(
                """
                SELECT COUNT(*)
                FROM inbox_events
                WHERE event_id = ?
                """.trimIndent(),
                Int::class.java,
                eventId,
            ),
        )

    private companion object {
        val FIXED_TIME:
                Instant =
            Instant.parse(
                "2026-08-09T00:00:00Z",
            )

        @Container
        @JvmField
        val postgres =
            PostgreSQLContainer(
                "postgres:17-alpine",
            )
    }
}