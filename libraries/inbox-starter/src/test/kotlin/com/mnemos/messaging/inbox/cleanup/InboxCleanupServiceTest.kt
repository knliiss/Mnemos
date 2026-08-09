package com.mnemos.messaging.inbox.cleanup

import com.mnemos.messaging.inbox.InboxClock
import com.mnemos.messaging.inbox.config.MnemosInboxProperties
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.time.Duration
import java.time.Instant

class InboxCleanupServiceTest {

    private val now = Instant.parse("2026-08-09T00:00:00Z")
    private val recordedBefore = Instant.parse("2026-07-10T00:00:00Z")

    @Test
    fun `cleanup deletes expired events in batches`() {
        val repository = mock<InboxCleanupRepository>()

        whenever(
            repository.deleteExpired(
                recordedBefore = recordedBefore,
                batchSize = 1_000,
            ),
        ).thenReturn(1_000, 1_000, 250)

        val service = InboxCleanupService(
            repository = repository,
            clock = { now },
            properties = properties(maxBatchesPerRun = 10),
        )

        assertEquals(2_250, service.cleanup())

        verify(repository, times(3)).deleteExpired(
            recordedBefore = recordedBefore,
            batchSize = 1_000,
        )
    }

    @Test
    fun `cleanup stops after configured maximum batches`() {
        val repository = mock<InboxCleanupRepository>()

        whenever(
            repository.deleteExpired(
                recordedBefore = recordedBefore,
                batchSize = 1_000,
            ),
        ).thenReturn(1_000)

        val service = InboxCleanupService(
            repository = repository,
            clock = { now },
            properties = properties(maxBatchesPerRun = 2),
        )

        assertEquals(2_000, service.cleanup())

        verify(repository, times(2)).deleteExpired(
            recordedBefore = recordedBefore,
            batchSize = 1_000,
        )
    }

    private fun properties(maxBatchesPerRun: Int) = MnemosInboxProperties.Cleanup(
        retention = Duration.ofDays(30),
        batchSize = 1_000,
        maxBatchesPerRun = maxBatchesPerRun,
    )
}