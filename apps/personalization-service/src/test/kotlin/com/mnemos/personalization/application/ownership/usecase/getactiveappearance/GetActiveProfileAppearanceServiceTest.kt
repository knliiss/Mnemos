package com.mnemos.personalization.application.ownership.usecase.getactiveappearance

import com.mnemos.common.core.model.id.AccountId
import com.mnemos.personalization.application.ownership.event.ProfileAppearanceSnapshotResolver
import com.mnemos.personalization.application.ownership.event.port.out.ProfileAppearanceRevisionStore
import com.mnemos.personalization.contracts.shared.ProfileAppearanceSnapshotV1
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset
import java.util.UUID

class GetActiveProfileAppearanceServiceTest {
    private val accountId = AccountId(UUID.fromString("019c1d10-0000-7000-8000-000000000001"))
    private val snapshotResolver = mock<ProfileAppearanceSnapshotResolver>()
    private val revisionStore = mock<ProfileAppearanceRevisionStore>()
    private val clock = Clock.fixed(NOW, ZoneOffset.UTC)
    private val service =
        GetActiveProfileAppearanceService(
            snapshotResolver = snapshotResolver,
            revisionStore = revisionStore,
            clock = clock,
        )

    @Test
    fun `retries when revision changes while snapshot is being read`() {
        val firstSnapshot = ProfileAppearanceSnapshotV1(null, null, null, null)
        val stableSnapshot =
            ProfileAppearanceSnapshotV1(
                nickStyle = null,
                profileTheme = null,
                avatarFrame = null,
                profileEffect = null,
                profileBackground =
                    ProfileAppearanceSnapshotV1.ProfileBackground(
                        personalizationId = UUID.fromString("019c1d10-0000-7000-8000-000000000002"),
                        effect = "AURORA",
                        intensity = 50,
                        primaryColor = "#101D0A",
                        secondaryColor = "#203D12",
                    ),
            )

        whenever(revisionStore.current(accountId)).thenReturn(1L, 2L, 2L, 2L)
        whenever(snapshotResolver.resolve(accountId)).thenReturn(firstSnapshot, stableSnapshot)

        val result = service.execute(accountId)

        assertEquals(2L, result.revision)
        assertSame(stableSnapshot, result.appearance)
        assertEquals(NOW, result.observedAt)
        verify(snapshotResolver, times(2)).resolve(accountId)
    }

    private companion object {
        val NOW: Instant = Instant.parse("2026-08-26T20:45:00Z")
    }
}
