package com.mnemos.social.adapter.inbound.http.accountreaction

import com.mnemos.social.application.actor.ResolveTelegramSocialActorUseCase
import com.mnemos.social.application.actor.model.SocialActor
import com.mnemos.social.application.accountreaction.usecase.get.GetAccountReactionForActorUseCase
import com.mnemos.social.application.accountreaction.usecase.remove.RemoveAccountReactionForActorUseCase
import com.mnemos.social.application.accountreaction.usecase.set.SetAccountReactionCommand
import com.mnemos.social.application.accountreaction.usecase.set.SetAccountReactionForActorUseCase
import com.mnemos.social.contracts.internal.v1.InternalTelegramReactionRequest
import com.mnemos.social.contracts.internal.v1.InternalTelegramReactionType
import com.mnemos.social.domain.reaction.model.AccountReactionType
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoInteractions
import org.mockito.kotlin.whenever
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.server.ResponseStatusException
import java.util.UUID

class InternalTelegramAccountReactionControllerTest {
    private val actors = mock<ResolveTelegramSocialActorUseCase>()
    private val get = mock<GetAccountReactionForActorUseCase>()
    private val set = mock<SetAccountReactionForActorUseCase>()
    private val remove = mock<RemoveAccountReactionForActorUseCase>()
    private val controller = InternalTelegramAccountReactionController(actors, get, set, remove)
    private val actor = SocialActor(UUID.randomUUID(), UUID.randomUUID(), "Kirito")
    private val target = UUID.randomUUID()

    @Test
    fun setCallsExistingReactionUseCaseForVerifiedActor() {
        whenever(actors.resolve(11L)).thenReturn(actor)

        controller.set(11L, target, InternalTelegramReactionRequest(InternalTelegramReactionType.LIKE))

        verify(set).executeForActor(SetAccountReactionCommand(target, AccountReactionType.LIKE), actor)
        verifyNoInteractions(remove)
    }

    @Test
    fun getAndDeleteUseTheSameResolvedActor() {
        whenever(actors.resolve(11L)).thenReturn(actor)
        whenever(get.executeForActor(target, actor)).thenReturn(AccountReactionType.DISLIKE)

        assertEquals(InternalTelegramReactionType.DISLIKE, controller.current(11L, target).type)
        controller.remove(11L, target)
        verify(remove).executeForActor(target, actor)
    }

    @Test
    fun unknownOrUnlinkedTelegramAccountCannotWrite() {
        val exception = assertThrows(ResponseStatusException::class.java) {
            controller.set(11L, target, InternalTelegramReactionRequest(InternalTelegramReactionType.DISLIKE))
        }
        assertEquals(404, exception.statusCode.value())
        verifyNoInteractions(set, remove, get)
    }

    @Test
    fun controllerIsRestrictedToAuthenticatedTelegramServicePrincipal() {
        val guard = InternalTelegramAccountReactionController::class.java.getAnnotation(PreAuthorize::class.java)
        assertNotNull(guard)
        assertEquals("authentication.name == 'telegram-bot-service'", guard.value)
    }
}
