package com.mnemos.profile.adapter.inbound.http.profile

import com.mnemos.profile.adapter.inbound.http.profile.InternalTelegramProfileMapper.toInternalSummary
import com.mnemos.profile.domain.profile.model.ProfilePlayerState
import com.mnemos.profile.domain.profile.model.PublicProfile
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import java.time.Instant
import java.util.UUID

class InternalTelegramProfileMapperTest {
    @Test
    fun privacyHiddenPlayerDoesNotLeakRealmOrLastSeenOnline() {
        val accountId = UUID.randomUUID()
        val state = ProfilePlayerState(
            playerId = UUID.randomUUID(),
            nickname = "Kirito",
            externalVersion = 1L,
            presenceStatus = ProfilePlayerState.PresenceStatus.ONLINE,
            realm = "HIDDEN-REALM",
            realmType = "SAO",
            privacyHidden = true,
            lastSeenOnline = Instant.EPOCH,
            presenceObservedAt = Instant.EPOCH,
            updatedAt = Instant.EPOCH,
        )
        val public = PublicProfile(
            accountId = accountId,
            cristalixUuid = UUID.randomUUID(),
            cristalixNickname = "Kirito",
            likes = 2,
            dislikes = 1,
            comments = 5,
            socialRevision = 1,
            createdAt = Instant.EPOCH,
            updatedAt = Instant.EPOCH,
            playerState = state,
        )

        val internal = public.toInternalSummary()

        assertEquals(accountId, internal.accountId)
        assertEquals("HIDDEN", internal.presenceStatus)
        assertNull(internal.realm)
        assertNull(internal.lastSeenOnline)
    }

    @Test
    fun offlinePresenceCanExposeLastSeenWhenNotPrivate() {
        val state = ProfilePlayerState(
            playerId = UUID.randomUUID(),
            nickname = "Asuna",
            externalVersion = 1L,
            presenceStatus = ProfilePlayerState.PresenceStatus.OFFLINE,
            realm = null,
            realmType = null,
            privacyHidden = false,
            lastSeenOnline = Instant.EPOCH,
            presenceObservedAt = Instant.EPOCH,
            updatedAt = Instant.EPOCH,
        )
        val public = PublicProfile(
            accountId = UUID.randomUUID(),
            cristalixUuid = UUID.randomUUID(),
            cristalixNickname = "Asuna",
            likes = 0,
            dislikes = 0,
            comments = 0,
            socialRevision = 0,
            createdAt = Instant.EPOCH,
            updatedAt = Instant.EPOCH,
            playerState = state,
        )

        val internal = public.toInternalSummary()

        assertEquals("OFFLINE", internal.presenceStatus)
        assertEquals(Instant.EPOCH, internal.lastSeenOnline)
    }
}
