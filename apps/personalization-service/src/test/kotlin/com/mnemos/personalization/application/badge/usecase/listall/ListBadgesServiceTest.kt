package com.mnemos.personalization.application.badge.usecase.listall

import com.mnemos.personalization.domain.badge.model.Badge
import com.mnemos.personalization.domain.badge.repository.BadgeRepository
import com.mnemos.personalization.domain.personalization.model.HexColor
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import java.time.Instant

class ListBadgesServiceTest {
    @Test
    fun `returns all badges from repository`() {
        val repository = mock<BadgeRepository>()
        val badges =
            listOf(
                badge(
                    code = "EARLY_SUPPORTER",
                    name = "Early Supporter",
                ),
                badge(
                    code = "EVENT_MASTER",
                    name = "Event Master",
                ),
            )
        val service = ListBadgesService(repository)

        whenever(repository.findAll()).thenReturn(badges)

        val result = service.execute()

        assertEquals(badges, result)
    }

    private fun badge(
        code: String,
        name: String,
    ): Badge =
        Badge.create(
            code = code,
            name = name,
            color = HexColor("#C7F24A"),
            now = Instant.parse("2026-08-19T18:00:00Z"),
        )
}
