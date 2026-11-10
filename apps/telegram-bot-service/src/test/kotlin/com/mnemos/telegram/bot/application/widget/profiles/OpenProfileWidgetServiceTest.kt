package com.mnemos.telegram.bot.application.widget.profiles

import com.mnemos.telegram.bot.application.message.model.TelegramDeliveryResult
import com.mnemos.telegram.bot.application.profile.model.ProfilePresenceStatus
import com.mnemos.telegram.bot.application.profile.model.PublicProfileDetailResult
import com.mnemos.telegram.bot.application.profile.model.PublicProfileSearchPage
import com.mnemos.telegram.bot.application.profile.model.PublicProfileSearchResult
import com.mnemos.telegram.bot.application.profile.model.PublicProfileSummary
import com.mnemos.telegram.bot.application.profile.port.out.PublicProfileGateway
import com.mnemos.telegram.bot.application.widget.profiles.port.out.ProfileWidgetPublisher
import com.mnemos.telegram.bot.configuration.widget.TelegramWidgetProperties
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verifyNoInteractions
import org.mockito.kotlin.whenever
import java.time.Instant
import java.util.UUID

class OpenProfileWidgetServiceTest {
    private val gateway = mock<PublicProfileGateway>()
    private val publisher = mock<ProfileWidgetPublisher>()
    private val service = OpenProfileWidgetService(TelegramWidgetProperties(enabled = true), gateway, publisher)
    private val accountId = UUID.randomUUID()

    @Test
    fun initialOpenDoesNotCallPublicProfileService() {
        val result = service.load(11L, null, 0) as ProfileWidgetSnapshotResult.Available
        assertEquals(null, result.view.query)
        assertEquals(emptyList<ProfileWidgetEntry>(), result.view.profiles)
        verifyNoInteractions(gateway)
    }

    @Test
    fun searchUsesTypedGatewayAndReturnsBoundedResults() {
        whenever(gateway.search("Kirito", 0, 4)).thenReturn(
            PublicProfileSearchResult.Available(PublicProfileSearchPage(listOf(profile()), 0, 1, 1)),
        )
        val result = service.load(11L, "Kirito", 0) as ProfileWidgetSnapshotResult.Available

        assertEquals("Kirito", result.view.query)
        assertEquals(1L, result.view.totalItems)
        assertEquals(accountId, result.view.profiles.single().accountId)
        assertEquals("SAO-2", result.view.profiles.single().realm)
    }

    @Test
    fun detailUsesExistingPublicProfileProjection() {
        whenever(gateway.get(accountId)).thenReturn(PublicProfileDetailResult.Available(profile()))
        val result = service.load(11L, "Kirito", 0, accountId) as ProfileWidgetSnapshotResult.Available

        assertEquals(accountId, result.view.selected?.accountId)
        assertEquals(emptyList<ProfileWidgetEntry>(), result.view.profiles)
    }

    @Test
    fun invalidSearchCannotReachInternalGateway() {
        assertEquals(ProfileWidgetSnapshotResult.Unavailable, service.load(11L, "a", 0))
        verifyNoInteractions(gateway)
    }

    @Test
    fun disabledWidgetNeverQueriesProfiles() {
        val disabled = OpenProfileWidgetService(TelegramWidgetProperties(enabled = false), gateway, publisher)
        assertEquals(TelegramDeliveryResult.PermanentFailure("widgets-disabled"), disabled.execute(11L))
        verifyNoInteractions(gateway)
    }

    private fun profile() = PublicProfileSummary(
        accountId, "Kirito", UUID.randomUUID(), 10, 2, 3,
        ProfilePresenceStatus.ONLINE, "SAO-2", Instant.EPOCH, Instant.EPOCH,
    )
}
