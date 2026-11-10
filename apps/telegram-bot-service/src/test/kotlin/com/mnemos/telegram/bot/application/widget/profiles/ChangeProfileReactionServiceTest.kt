package com.mnemos.telegram.bot.application.widget.profiles

import com.mnemos.telegram.bot.application.profilereaction.ProfileReactionChangeResult
import com.mnemos.telegram.bot.application.profilereaction.ProfileReactionState
import com.mnemos.telegram.bot.application.profilereaction.ProfileReactionType
import com.mnemos.telegram.bot.application.profilereaction.port.out.ProfileReactionGateway
import com.mnemos.telegram.bot.application.widget.profiles.port.out.ProfileReactionQueryGuard
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoInteractions
import org.mockito.kotlin.whenever
import java.util.UUID

class ChangeProfileReactionServiceTest {
    private val reactions = mock<ProfileReactionGateway>()
    private val guard = mock<ProfileReactionQueryGuard>()
    private val service = ChangeProfileReactionService(reactions, guard)
    private val accountId = UUID.randomUUID()

    @Test
    fun desiredLikeIsAppliedOnlyAfterAccountResolutionAndUniqueQueryClaim() {
        whenever(reactions.get(11L, accountId)).thenReturn(ProfileReactionState.Available(null))
        whenever(guard.claim(11L, "q")).thenReturn(true)
        whenever(reactions.set(11L, accountId, ProfileReactionType.LIKE))
            .thenReturn(ProfileReactionChangeResult.APPLIED)

        assertEquals(
            ProfileReactionChangeResult.APPLIED,
            service.execute(11L, accountId, ProfileReactionType.LIKE, "q"),
        )
        verify(reactions).set(11L, accountId, ProfileReactionType.LIKE)
    }

    @Test
    fun alreadyAppliedDesiredStateDoesNotWriteAgain() {
        whenever(reactions.get(11L, accountId))
            .thenReturn(ProfileReactionState.Available(ProfileReactionType.DISLIKE))
        whenever(guard.claim(11L, "q")).thenReturn(true)

        assertEquals(
            ProfileReactionChangeResult.APPLIED,
            service.execute(11L, accountId, ProfileReactionType.DISLIKE, "q"),
        )
        verify(reactions).get(11L, accountId)
        verify(guard).claim(11L, "q")
    }

    @Test
    fun repeatQueryCannotReverseReactionAfterAnotherUpdate() {
        whenever(reactions.get(11L, accountId)).thenReturn(
            ProfileReactionState.Available(ProfileReactionType.DISLIKE),
        )
        whenever(guard.claim(11L, "stale-q")).thenReturn(false)

        assertEquals(
            ProfileReactionChangeResult.INVALID,
            service.execute(11L, accountId, ProfileReactionType.LIKE, "stale-q"),
        )
        verify(reactions).get(11L, accountId)
        org.mockito.kotlin.verify(reactions, org.mockito.kotlin.never()).set(11L, accountId, ProfileReactionType.LIKE)
    }

    @Test
    fun unlinkedUserCannotWrite() {
        whenever(reactions.get(11L, accountId)).thenReturn(ProfileReactionState.AccountRequired)

        assertEquals(
            ProfileReactionChangeResult.ACCOUNT_REQUIRED,
            service.execute(11L, accountId, ProfileReactionType.LIKE, "q"),
        )
        verifyNoInteractions(guard)
    }

    @Test
    fun unavailableSocialServiceDoesNotClaimMutation() {
        whenever(reactions.get(11L, accountId)).thenReturn(ProfileReactionState.Unavailable)

        assertEquals(
            ProfileReactionChangeResult.UNAVAILABLE,
            service.execute(11L, accountId, null, "q"),
        )
        verifyNoInteractions(guard)
    }
}
