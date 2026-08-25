package com.mnemos.event.adapter.out.persistence.wipe

import com.mnemos.event.domain.wipe.model.Wipe
import com.mnemos.event.domain.wipe.model.WipeId
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.time.Instant
import java.util.UUID

class WipeRepositoryAdapterTest {
    private val jpaRepository = mock<WipeJpaRepository>()
    private val repository = WipeRepositoryAdapter(jpaRepository)

    @Test
    fun `save flushes wipe state before another wipe can be persisted`() {
        val wipe =
            Wipe(
                id = WipeId(UUID.fromString("019c1e23-7000-7000-8000-000000000001")),
                number = 1,
                name = "Legacy",
                startedAt = Instant.parse("1970-01-01T00:00:00Z"),
                endedAt = Instant.parse("2026-08-25T06:30:00Z"),
                createdBy = "system:migration",
                createdAt = Instant.parse("2026-08-24T00:00:00Z"),
            )

        whenever(jpaRepository.saveAndFlush(any<WipeEntity>())).thenAnswer { invocation ->
            invocation.getArgument<WipeEntity>(0)
        }

        val saved = repository.save(wipe)

        assertEquals(wipe, saved)
        verify(jpaRepository).saveAndFlush(any<WipeEntity>())
        verify(jpaRepository, never()).save(any<WipeEntity>())
    }
}
