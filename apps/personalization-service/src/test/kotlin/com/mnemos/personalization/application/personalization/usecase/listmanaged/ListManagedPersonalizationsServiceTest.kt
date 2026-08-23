package com.mnemos.personalization.application.personalization.usecase.listmanaged

import com.mnemos.personalization.application.personalization.support.PersonalizationModerationSupport
import com.mnemos.personalization.domain.personalization.model.Personalization
import com.mnemos.personalization.domain.personalization.model.PersonalizationStatus
import com.mnemos.personalization.domain.personalization.repository.PersonalizationRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.time.Instant

class ListManagedPersonalizationsServiceTest {
    @Test
    fun `list returns published and unlisted personalizations ordered by update time`() {
        val support = mock<PersonalizationModerationSupport>()
        val repository = mock<PersonalizationRepository>()
        val olderPublished = mock<Personalization>()
        val newestUnlisted = mock<Personalization>()
        val newerPublished = mock<Personalization>()

        whenever(olderPublished.updatedAt).thenReturn(Instant.parse("2026-08-20T10:00:00Z"))
        whenever(newestUnlisted.updatedAt).thenReturn(Instant.parse("2026-08-20T12:00:00Z"))
        whenever(newerPublished.updatedAt).thenReturn(Instant.parse("2026-08-20T11:00:00Z"))
        whenever(repository.findByStatus(PersonalizationStatus.PUBLISHED)).thenReturn(
            listOf(olderPublished, newerPublished),
        )
        whenever(repository.findByStatus(PersonalizationStatus.UNLISTED)).thenReturn(
            listOf(newestUnlisted),
        )

        val service =
            ListManagedPersonalizationsService(
                support = support,
                repository = repository,
            )

        val result = service.execute()

        assertEquals(
            listOf(newestUnlisted, newerPublished, olderPublished),
            result,
        )
        verify(support).requireModerator()
        verify(repository).findByStatus(PersonalizationStatus.PUBLISHED)
        verify(repository).findByStatus(PersonalizationStatus.UNLISTED)
    }
}
