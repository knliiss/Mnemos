package com.mnemos.messaging.outbox.cleanup

import com.mnemos.messaging.outbox.config.MnemosOutboxProperties
import com.mnemos.messaging.outbox.support.OutboxClock
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.mockito.kotlin.mock
import java.time.Duration
import java.time.Instant

class OutboxCleanupServiceTest {

    private val now =
        Instant.parse("2026-07-27T18:00:00Z")

    private val publishedBefore =
        Instant.parse("2026-07-20T18:00:00Z")

    private val deadBefore =
        Instant.parse("2026-06-27T18:00:00Z")

    @Test
    fun `cleanup deletes expired events in batches`() {
        val repository = mock<OutboxCleanupRepository>()

        whenever(
            repository.deleteExpired(
                publishedBefore = publishedBefore,
                deadBefore = deadBefore,
                batchSize = 100
            )
        ).thenReturn(100, 100, 25)

        val service = OutboxCleanupService(
            repository = repository,
            clock = OutboxClock { now },
            properties = cleanupProperties(
                batchSize = 100,
                maxBatchesPerRun = 10
            )
        )

        val deleted = service.cleanup()

        assertEquals(225, deleted)

        verify(repository, times(3)).deleteExpired(
            publishedBefore = publishedBefore,
            deadBefore = deadBefore,
            batchSize = 100
        )
    }

    @Test
    fun `cleanup stops after configured maximum batches`() {
        val repository = mock<OutboxCleanupRepository>()

        whenever(
            repository.deleteExpired(
                publishedBefore = publishedBefore,
                deadBefore = deadBefore,
                batchSize = 100
            )
        ).thenReturn(100)

        val service = OutboxCleanupService(
            repository = repository,
            clock = OutboxClock { now },
            properties = cleanupProperties(
                batchSize = 100,
                maxBatchesPerRun = 2
            )
        )

        val deleted = service.cleanup()

        assertEquals(200, deleted)

        verify(repository, times(2)).deleteExpired(
            publishedBefore = publishedBefore,
            deadBefore = deadBefore,
            batchSize = 100
        )
    }

    @Test
    fun `cleanup stops after incomplete batch`() {
        val repository = mock<OutboxCleanupRepository>()

        whenever(
            repository.deleteExpired(
                publishedBefore = publishedBefore,
                deadBefore = deadBefore,
                batchSize = 100
            )
        ).thenReturn(0)

        val service = OutboxCleanupService(
            repository = repository,
            clock = OutboxClock { now },
            properties = cleanupProperties(
                batchSize = 100,
                maxBatchesPerRun = 10
            )
        )

        val deleted = service.cleanup()

        assertEquals(0, deleted)

        verify(repository).deleteExpired(
            publishedBefore = publishedBefore,
            deadBefore = deadBefore,
            batchSize = 100
        )
    }

    private fun cleanupProperties(
        batchSize: Int,
        maxBatchesPerRun: Int
    ): MnemosOutboxProperties.Cleanup =
        MnemosOutboxProperties.Cleanup(
            enabled = true,
            initialDelay = Duration.ofMinutes(10),
            fixedDelay = Duration.ofHours(1),
            publishedRetention = Duration.ofDays(7),
            deadRetention = Duration.ofDays(30),
            batchSize = batchSize,
            maxBatchesPerRun = maxBatchesPerRun
        )
}