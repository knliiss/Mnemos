package com.mnemos.social.application.actor

import com.mnemos.social.application.actor.model.SocialActor
import com.mnemos.social.application.actor.model.TelegramSocialIdentity
import com.mnemos.social.application.actor.port.out.AccountAccessPort
import com.mnemos.social.application.actor.port.out.TelegramSocialIdentityPort
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verifyNoInteractions
import org.mockito.kotlin.whenever
import java.util.UUID

class ResolveTelegramSocialActorServiceTest {
    private val identities = mock<TelegramSocialIdentityPort>()
    private val accounts = mock<AccountAccessPort>()
    private val resolver = ResolveTelegramSocialActorService(identities, accounts)
    private val userId = UUID.randomUUID()
    private val accountId = UUID.randomUUID()

    @Test
    fun resolvesOnlyUserBoundToTelegramAndActiveAccount() {
        whenever(identities.resolve(11L)).thenReturn(TelegramSocialIdentity(userId, accountId))
        val actor = SocialActor(userId, accountId, "Kirito")
        whenever(accounts.resolveActiveActor(userId)).thenReturn(actor)

        assertEquals(actor, resolver.resolve(11L))
    }

    @Test
    fun rejectsAnotherActiveAccountEvenForCorrectLinkedUser() {
        whenever(identities.resolve(11L)).thenReturn(TelegramSocialIdentity(userId, accountId))
        whenever(accounts.resolveActiveActor(userId))
            .thenReturn(SocialActor(userId, UUID.randomUUID(), "Other"))

        assertNull(resolver.resolve(11L))
    }

    @Test
    fun missingTelegramIdentityNeverResolvesArbitraryUser() {
        assertNull(resolver.resolve(11L))
        verifyNoInteractions(accounts)
    }

    @Test
    fun linkedUserWithoutActiveAccountCannotMutateSocialData() {
        whenever(identities.resolve(11L)).thenReturn(TelegramSocialIdentity(userId, null))

        assertNull(resolver.resolve(11L))
        verifyNoInteractions(accounts)
    }

    @Test
    fun invalidTelegramIdFailsClosed() {
        assertNull(resolver.resolve(0))
        verifyNoInteractions(identities, accounts)
    }
}
