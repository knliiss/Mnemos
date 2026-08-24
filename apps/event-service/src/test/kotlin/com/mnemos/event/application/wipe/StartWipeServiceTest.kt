package com.mnemos.event.application.wipe

import com.mnemos.event.domain.wipe.exception.WipeValidationException
import com.mnemos.event.domain.wipe.model.Wipe
import com.mnemos.event.domain.wipe.model.WipeId
import com.mnemos.event.domain.wipe.repository.WipeRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset

class StartWipeServiceTest {
    private val now = Instant.parse("2026-08-24T12:00:00Z")
    private val clock = Clock.fixed(now, ZoneOffset.UTC)

    @Test
    fun `starting wipe closes current wipe on the same boundary`() {
        val current = wipe(number = 4, startedAt = Instant.parse("2026-08-01T00:00:00Z"))
        val repository = FakeWipeRepository(current)
        val service = StartWipeService(repository, clock)
        val boundary = Instant.parse("2026-08-20T08:30:00Z")

        val created =
            service.execute(
                name = "  September  ",
                startedAt = boundary,
                createdBy = "admin-user",
            )

        assertEquals(1, repository.findCurrentForUpdateCalls)
        assertEquals(2, repository.saved.size)
        assertEquals(boundary, repository.saved[0].endedAt)
        assertEquals(5, created.number)
        assertEquals("September", created.name)
        assertEquals(boundary, created.startedAt)
        assertEquals(null, created.endedAt)
        assertEquals("admin-user", created.createdBy)
        assertEquals(now, created.createdAt)
    }

    @Test
    fun `starting wipe in the future is rejected without persistence`() {
        val current = wipe(number = 1, startedAt = Instant.parse("2026-08-01T00:00:00Z"))
        val repository = FakeWipeRepository(current)
        val service = StartWipeService(repository, clock)

        assertThrows(WipeValidationException::class.java) {
            service.execute(
                name = null,
                startedAt = now.plusSeconds(1),
                createdBy = "admin-user",
            )
        }

        assertEquals(0, repository.findCurrentForUpdateCalls)
        assertEquals(emptyList<Wipe>(), repository.saved)
    }

    @Test
    fun `starting wipe before current boundary is rejected`() {
        val currentStart = Instant.parse("2026-08-10T00:00:00Z")
        val repository = FakeWipeRepository(wipe(number = 2, startedAt = currentStart))
        val service = StartWipeService(repository, clock)

        assertThrows(WipeValidationException::class.java) {
            service.execute(
                name = "Invalid",
                startedAt = currentStart,
                createdBy = "admin-user",
            )
        }

        assertEquals(1, repository.findCurrentForUpdateCalls)
        assertEquals(emptyList<Wipe>(), repository.saved)
    }

    private fun wipe(
        number: Int,
        startedAt: Instant,
    ): Wipe =
        Wipe(
            id = WipeId.generate(),
            number = number,
            name = "Wipe $number",
            startedAt = startedAt,
            endedAt = null,
            createdBy = "system",
            createdAt = startedAt,
        )

    private class FakeWipeRepository(
        private var current: Wipe?,
    ) : WipeRepository {
        val saved = mutableListOf<Wipe>()
        var findCurrentForUpdateCalls = 0

        override fun findCurrent(): Wipe? = current

        override fun findCurrentForUpdate(): Wipe? {
            findCurrentForUpdateCalls += 1

            return current
        }

        override fun findById(id: WipeId): Wipe? =
            listOfNotNull(current)
                .plus(saved)
                .firstOrNull { wipe -> wipe.id == id }

        override fun findAll(): List<Wipe> = listOfNotNull(current).plus(saved)

        override fun save(wipe: Wipe): Wipe {
            saved += wipe

            if (wipe.active) {
                current = wipe
            }

            return wipe
        }
    }
}
