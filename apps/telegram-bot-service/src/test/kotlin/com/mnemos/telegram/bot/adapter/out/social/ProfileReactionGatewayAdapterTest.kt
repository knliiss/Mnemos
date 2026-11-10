package com.mnemos.telegram.bot.adapter.out.social

import com.mnemos.social.contracts.internal.v1.InternalTelegramReactionRequest
import com.mnemos.social.contracts.internal.v1.InternalTelegramReactionState
import com.mnemos.social.contracts.internal.v1.InternalTelegramReactionType
import com.mnemos.telegram.bot.application.profilereaction.ProfileReactionChangeResult
import com.mnemos.telegram.bot.application.profilereaction.ProfileReactionState
import com.mnemos.telegram.bot.application.profilereaction.ProfileReactionType
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.util.UUID

class ProfileReactionGatewayAdapterTest {
    private val client = mock<SocialServiceClient>()
    private val gateway = ProfileReactionGatewayAdapter(client)
    private val accountId = UUID.randomUUID()

    @Test
    fun currentReactionMapsOnlyTheVerifiedTelegramActorsState() {
        whenever(client.getReaction(11L, accountId))
            .thenReturn(InternalTelegramReactionState(InternalTelegramReactionType.DISLIKE))

        assertEquals(
            ProfileReactionState.Available(ProfileReactionType.DISLIKE),
            gateway.get(11L, accountId),
        )
        verify(client).getReaction(11L, accountId)
    }

    @Test
    fun likeUsesIdempotentDesiredStateEndpoint() {
        assertEquals(
            ProfileReactionChangeResult.APPLIED,
            gateway.set(11L, accountId, ProfileReactionType.LIKE),
        )
        verify(client).setReaction(
            11L, accountId, InternalTelegramReactionRequest(InternalTelegramReactionType.LIKE),
        )
    }

    @Test
    fun removeUsesDeleteWithoutForgingAnActorId() {
        assertEquals(ProfileReactionChangeResult.APPLIED, gateway.set(11L, accountId, null))
        verify(client).removeReaction(11L, accountId)
    }
}
