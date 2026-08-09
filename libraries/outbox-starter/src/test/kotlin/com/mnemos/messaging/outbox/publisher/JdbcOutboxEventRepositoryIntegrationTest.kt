package com.mnemos.messaging.outbox.publisher

import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.springframework.core.io.ClassPathResource
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.jdbc.datasource.DataSourceTransactionManager
import org.springframework.jdbc.datasource.DriverManagerDataSource
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator
import org.springframework.transaction.support.TransactionTemplate
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import org.testcontainers.postgresql.PostgreSQLContainer
import java.time.Instant
import java.time.ZoneOffset
import java.util.UUID
import java.util.concurrent.CyclicBarrier
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

@Testcontainers
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class JdbcOutboxEventRepositoryIntegrationTest {

    private lateinit var jdbcOperations:
            NamedParameterJdbcTemplate

    private lateinit var repository:
            JdbcOutboxEventRepository

    private val executors =
        mutableListOf<ExecutorService>()

    @BeforeAll
    fun setUpInfrastructure() {
        val dataSource =
            DriverManagerDataSource(
                postgres.jdbcUrl,
                postgres.username,
                postgres.password,
            )

        ResourceDatabasePopulator(
            ClassPathResource(
                "db/outbox-schema.sql",
            ),
        ).execute(dataSource)

        jdbcOperations =
            NamedParameterJdbcTemplate(dataSource)

        repository =
            JdbcOutboxEventRepository(
                jdbcOperations = jdbcOperations,
                transactionOperations =
                    TransactionTemplate(
                        DataSourceTransactionManager(
                            dataSource,
                        ),
                    ),
            )
    }

    @BeforeEach
    fun clearOutbox() {
        jdbcOperations.update(
            "DELETE FROM outbox_events",
            emptyMap<String, Any>(),
        )
    }

    @AfterEach
    fun shutDownExecutors() {
        executors.forEach(
            ExecutorService::shutdownNow,
        )

        executors.clear()
    }

    @Test
    fun `claim returns oldest eligible event`() {
        val first =
            insertEvent(
                createdAt =
                    NOW.minusSeconds(2),
                nextAttemptAt =
                    NOW.minusSeconds(1),
            )

        insertEvent(
            createdAt =
                NOW.minusSeconds(1),
            nextAttemptAt =
                NOW.minusSeconds(1),
        )

        val claimed =
            requireNotNull(
                repository.claimNextPending(
                    instanceId = INSTANCE_A,
                    now = NOW,
                    lockedUntil =
                        NOW.plusSeconds(30),
                ),
            )

        assertEquals(
            first,
            claimed.id,
        )
    }

    @Test
    fun `leased event cannot be claimed by another publisher`() {
        val eventId =
            insertEvent(
                createdAt =
                    NOW.minusSeconds(1),
                nextAttemptAt = NOW,
            )

        val claimed =
            requireNotNull(
                repository.claimNextPending(
                    instanceId = INSTANCE_A,
                    now = NOW,
                    lockedUntil =
                        NOW.plusSeconds(30),
                ),
            )

        assertEquals(
            eventId,
            claimed.id,
        )

        val secondClaim =
            repository.claimNextPending(
                instanceId = INSTANCE_B,
                now = NOW.plusSeconds(10),
                lockedUntil =
                    NOW.plusSeconds(40),
            )

        assertNull(secondClaim)
    }

    @Test
    fun `expired lease can be reclaimed`() {
        val eventId =
            insertEvent(
                createdAt =
                    NOW.minusSeconds(1),
                nextAttemptAt = NOW,
            )

        repository.claimNextPending(
            instanceId = INSTANCE_A,
            now = NOW,
            lockedUntil =
                NOW.plusSeconds(30),
        )

        val reclaimed =
            requireNotNull(
                repository.claimNextPending(
                    instanceId = INSTANCE_B,
                    now = NOW.plusSeconds(31),
                    lockedUntil =
                        NOW.plusSeconds(61),
                ),
            )

        assertEquals(
            eventId,
            reclaimed.id,
        )

        assertEquals(
            INSTANCE_B,
            lockedBy(eventId),
        )
    }

    @Test
    fun `later event of same aggregate waits for earlier pending event`() {
        val aggregateId =
            UUID.randomUUID()

        val first =
            insertEvent(
                aggregateId = aggregateId,
                createdAt =
                    NOW.minusSeconds(2),
                nextAttemptAt =
                    NOW.plusSeconds(30),
            )

        val second =
            insertEvent(
                aggregateId = aggregateId,
                createdAt =
                    NOW.minusSeconds(1),
                nextAttemptAt = NOW,
            )

        val blocked =
            repository.claimNextPending(
                instanceId = INSTANCE_A,
                now = NOW,
                lockedUntil =
                    NOW.plusSeconds(30),
            )

        assertNull(blocked)

        makeEligible(
            eventId = first,
            at = NOW,
        )

        val firstClaim =
            requireNotNull(
                repository.claimNextPending(
                    instanceId = INSTANCE_A,
                    now = NOW,
                    lockedUntil =
                        NOW.plusSeconds(30),
                ),
            )

        assertEquals(
            first,
            firstClaim.id,
        )

        assertTrue(
            repository.markPublished(
                eventId = first,
                instanceId = INSTANCE_A,
                publishedAt =
                    NOW.plusSeconds(1),
            ),
        )

        val secondClaim =
            requireNotNull(
                repository.claimNextPending(
                    instanceId = INSTANCE_A,
                    now = NOW.plusSeconds(2),
                    lockedUntil =
                        NOW.plusSeconds(32),
                ),
            )

        assertEquals(
            second,
            secondClaim.id,
        )
    }

    @Test
    fun `events with identical timestamps preserve insertion order within aggregate`() {
        val aggregateId =
            UUID.randomUUID()

        val first =
            insertEvent(
                aggregateId = aggregateId,
                createdAt = NOW,
                nextAttemptAt = NOW,
            )

        val second =
            insertEvent(
                aggregateId = aggregateId,
                createdAt = NOW,
                nextAttemptAt = NOW,
            )

        val firstClaim =
            requireNotNull(
                repository.claimNextPending(
                    instanceId = INSTANCE_A,
                    now = NOW,
                    lockedUntil =
                        NOW.plusSeconds(30),
                ),
            )

        assertEquals(
            first,
            firstClaim.id,
        )

        assertTrue(
            repository.markPublished(
                eventId = first,
                instanceId = INSTANCE_A,
                publishedAt = NOW,
            ),
        )

        val secondClaim =
            requireNotNull(
                repository.claimNextPending(
                    instanceId = INSTANCE_A,
                    now = NOW,
                    lockedUntil =
                        NOW.plusSeconds(30),
                ),
            )

        assertEquals(
            second,
            secondClaim.id,
        )
    }

    @Test
    fun `delayed event does not block another aggregate`() {
        insertEvent(
            aggregateId = UUID.randomUUID(),
            createdAt =
                NOW.minusSeconds(2),
            nextAttemptAt =
                NOW.plusSeconds(30),
        )

        val ready =
            insertEvent(
                aggregateId =
                    UUID.randomUUID(),
                createdAt =
                    NOW.minusSeconds(1),
                nextAttemptAt = NOW,
            )

        val claimed =
            requireNotNull(
                repository.claimNextPending(
                    instanceId = INSTANCE_A,
                    now = NOW,
                    lockedUntil =
                        NOW.plusSeconds(30),
                ),
            )

        assertEquals(
            ready,
            claimed.id,
        )
    }

    @Test
    fun `failed event is unavailable until next attempt`() {
        val eventId =
            insertEvent(
                createdAt =
                    NOW.minusSeconds(1),
                nextAttemptAt = NOW,
            )

        val claimed =
            requireNotNull(
                repository.claimNextPending(
                    instanceId = INSTANCE_A,
                    now = NOW,
                    lockedUntil =
                        NOW.plusSeconds(30),
                ),
            )

        assertEquals(
            0,
            claimed.failureCount,
        )

        assertTrue(
            repository.markFailed(
                eventId = eventId,
                instanceId = INSTANCE_A,
                previousFailureCount = 0,
                failureCount = 1,
                nextAttemptAt =
                    NOW.plusSeconds(10),
                error = "Kafka unavailable",
            ),
        )

        val beforeRetry =
            repository.claimNextPending(
                instanceId = INSTANCE_B,
                now = NOW.plusSeconds(9),
                lockedUntil =
                    NOW.plusSeconds(39),
            )

        assertNull(beforeRetry)

        val retry =
            requireNotNull(
                repository.claimNextPending(
                    instanceId = INSTANCE_B,
                    now = NOW.plusSeconds(10),
                    lockedUntil =
                        NOW.plusSeconds(40),
                ),
            )

        assertEquals(
            eventId,
            retry.id,
        )

        assertEquals(
            1,
            retry.failureCount,
        )
    }

    @Test
    fun `only lease owner can mark event published`() {
        val eventId =
            insertEvent(
                createdAt =
                    NOW.minusSeconds(1),
                nextAttemptAt = NOW,
            )

        repository.claimNextPending(
            instanceId = INSTANCE_A,
            now = NOW,
            lockedUntil =
                NOW.plusSeconds(30),
        )

        val wrongOwner =
            repository.markPublished(
                eventId = eventId,
                instanceId = INSTANCE_B,
                publishedAt =
                    NOW.plusSeconds(1),
            )

        assertFalse(wrongOwner)

        val correctOwner =
            repository.markPublished(
                eventId = eventId,
                instanceId = INSTANCE_A,
                publishedAt =
                    NOW.plusSeconds(1),
            )

        assertTrue(correctOwner)

        assertEquals(
            "PUBLISHED",
            status(eventId),
        )

        assertNull(
            lockedBy(eventId),
        )
    }

    @Test
    fun `mark failed rejects stale failure count`() {
        val eventId =
            insertEvent(
                createdAt =
                    NOW.minusSeconds(1),
                nextAttemptAt = NOW,
            )

        repository.claimNextPending(
            instanceId = INSTANCE_A,
            now = NOW,
            lockedUntil =
                NOW.plusSeconds(30),
        )

        val updated =
            repository.markFailed(
                eventId = eventId,
                instanceId = INSTANCE_A,
                previousFailureCount = 1,
                failureCount = 2,
                nextAttemptAt =
                    NOW.plusSeconds(10),
                error = "Kafka unavailable",
            )

        assertFalse(updated)

        assertEquals(
            0,
            failureCount(eventId),
        )
    }

    @Test
    fun `mark dead changes state and releases lease`() {
        val eventId =
            insertEvent(
                createdAt =
                    NOW.minusSeconds(1),
                nextAttemptAt = NOW,
            )

        repository.claimNextPending(
            instanceId = INSTANCE_A,
            now = NOW,
            lockedUntil =
                NOW.plusSeconds(30),
        )

        val updated =
            repository.markDead(
                eventId = eventId,
                instanceId = INSTANCE_A,
                previousFailureCount = 0,
                failureCount = 1,
                deadAt =
                    NOW.plusSeconds(1),
                error = "Permanent failure",
            )

        assertTrue(updated)

        assertEquals(
            "DEAD",
            status(eventId),
        )

        assertEquals(
            1,
            failureCount(eventId),
        )

        assertNull(
            lockedBy(eventId),
        )
    }

    @Test
    fun `two publishers never claim same event`() {
        val first =
            insertEvent(
                aggregateId =
                    UUID.randomUUID(),
                createdAt =
                    NOW.minusSeconds(2),
                nextAttemptAt = NOW,
            )

        val second =
            insertEvent(
                aggregateId =
                    UUID.randomUUID(),
                createdAt =
                    NOW.minusSeconds(1),
                nextAttemptAt = NOW,
            )

        val executor =
            Executors
                .newFixedThreadPool(2)
                .also(executors::add)

        val barrier =
            CyclicBarrier(2)

        val firstFuture =
            executor.submit<UUID?> {
                barrier.await(
                    5,
                    TimeUnit.SECONDS,
                )

                repository
                    .claimNextPending(
                        instanceId = INSTANCE_A,
                        now = NOW,
                        lockedUntil =
                            NOW.plusSeconds(30),
                    )
                    ?.id
            }

        val secondFuture =
            executor.submit<UUID?> {
                barrier.await(
                    5,
                    TimeUnit.SECONDS,
                )

                repository
                    .claimNextPending(
                        instanceId = INSTANCE_B,
                        now = NOW,
                        lockedUntil =
                            NOW.plusSeconds(30),
                    )
                    ?.id
            }

        val claimed =
            setOf(
                requireNotNull(
                    firstFuture.get(
                        5,
                        TimeUnit.SECONDS,
                    ),
                ),
                requireNotNull(
                    secondFuture.get(
                        5,
                        TimeUnit.SECONDS,
                    ),
                ),
            )

        assertEquals(
            setOf(
                first,
                second,
            ),
            claimed,
        )
    }

    private fun insertEvent(
        id: UUID = UUID.randomUUID(),
        aggregateId: UUID = UUID.randomUUID(),
        createdAt: Instant,
        nextAttemptAt: Instant,
    ): UUID {
        val parameters =
            MapSqlParameterSource()
                .addValue(
                    "id",
                    id,
                )
                .addValue(
                    "topic",
                    "mnemos.account.events.v1",
                )
                .addValue(
                    "eventKey",
                    aggregateId.toString(),
                )
                .addValue(
                    "eventType",
                    "account.created",
                )
                .addValue(
                    "eventVersion",
                    1,
                )
                .addValue(
                    "producer",
                    "account-service",
                )
                .addValue(
                    "aggregateType",
                    "ACCOUNT",
                )
                .addValue(
                    "aggregateId",
                    aggregateId,
                )
                .addValue(
                    "envelope",
                    """
                    {
                      "eventId": "$id",
                      "eventType": "account.created"
                    }
                    """.trimIndent(),
                )
                .addValue(
                    "occurredAt",
                    createdAt.atOffset(
                        ZoneOffset.UTC,
                    ),
                )
                .addValue(
                    "createdAt",
                    createdAt.atOffset(
                        ZoneOffset.UTC,
                    ),
                )
                .addValue(
                    "nextAttemptAt",
                    nextAttemptAt.atOffset(
                        ZoneOffset.UTC,
                    ),
                )

        jdbcOperations.update(
            """
            INSERT INTO outbox_events (
                id,
                topic,
                event_key,
                event_type,
                event_version,
                producer,
                aggregate_type,
                aggregate_id,
                envelope,
                occurred_at,
                created_at,
                next_attempt_at
            )
            VALUES (
                :id,
                :topic,
                :eventKey,
                :eventType,
                :eventVersion,
                :producer,
                :aggregateType,
                :aggregateId,
                CAST(:envelope AS JSONB),
                :occurredAt,
                :createdAt,
                :nextAttemptAt
            )
            """.trimIndent(),
            parameters,
        )

        return id
    }

    private fun makeEligible(
        eventId: UUID,
        at: Instant,
    ) {
        jdbcOperations.update(
            """
            UPDATE outbox_events
            SET next_attempt_at = :nextAttemptAt
            WHERE id = :eventId
            """.trimIndent(),
            MapSqlParameterSource()
                .addValue(
                    "eventId",
                    eventId,
                )
                .addValue(
                    "nextAttemptAt",
                    at.atOffset(
                        ZoneOffset.UTC,
                    ),
                ),
        )
    }

    private fun status(
        eventId: UUID,
    ): String? =
        jdbcOperations.queryForObject(
            """
            SELECT status
            FROM outbox_events
            WHERE id = :eventId
            """.trimIndent(),
            mapOf(
                "eventId" to eventId,
            ),
            String::class.java,
        )

    private fun lockedBy(
        eventId: UUID,
    ): String? =
        jdbcOperations.queryForObject(
            """
            SELECT locked_by
            FROM outbox_events
            WHERE id = :eventId
            """.trimIndent(),
            mapOf(
                "eventId" to eventId,
            ),
            String::class.java,
        )

    private fun failureCount(
        eventId: UUID,
    ): Int =
        requireNotNull(
            jdbcOperations.queryForObject(
                """
                SELECT failure_count
                FROM outbox_events
                WHERE id = :eventId
                """.trimIndent(),
                mapOf(
                    "eventId" to eventId,
                ),
                Int::class.java,
            ),
        )

    private companion object {

        val NOW: Instant =
            Instant.parse(
                "2026-08-08T18:00:00Z",
            )

        const val INSTANCE_A =
            "account-service:instance-a"

        const val INSTANCE_B =
            "account-service:instance-b"

        @Container
        @JvmField
        val postgres =
            PostgreSQLContainer(
                "postgres:17-alpine",
            )
    }
}