package com.mnemos.telegram.bot.adapter.out.profile

import com.mnemos.contracts.profile.internal.v1.InternalTelegramPublicProfileSummary
import com.mnemos.telegram.bot.application.profile.model.ProfilePresenceStatus
import com.mnemos.telegram.bot.application.profile.model.PublicProfileDetailResult
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import java.time.Instant
import java.util.UUID

class ProfileGatewayAdapterTest {
    private val client = mock<ProfileServiceClient>()
    private val adapter = ProfileGatewayAdapter(client)

    @Test
    fun hiddenPresenceNeverExposesRealmEvenIfUpstreamBreaksPrivacyContract() {
        val id = UUID.randomUUID()
        whenever(client.get(id)).thenReturn(
            InternalTelegramPublicProfileSummary(
                id, UUID.randomUUID(), "Kirito", 7, 2, 5,
                "HIDDEN", "SECRET-REALM", Instant.EPOCH, Instant.EPOCH,
            ),
        )
        val response = adapter.get(id) as PublicProfileDetailResult.Available
        assertEquals(ProfilePresenceStatus.HIDDEN, response.profile.presenceStatus)
        assertEquals(null, response.profile.realm)
        assertEquals(null, response.profile.lastSeenOnline)
    }
}
