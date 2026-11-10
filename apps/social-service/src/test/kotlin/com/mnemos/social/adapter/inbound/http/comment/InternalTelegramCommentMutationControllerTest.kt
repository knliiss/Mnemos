package com.mnemos.social.adapter.inbound.http.comment

import com.mnemos.social.application.actor.ResolveTelegramSocialActorUseCase
import com.mnemos.social.application.actor.model.SocialActor
import com.mnemos.social.application.comment.telegram.CreateTelegramCommentUseCase
import com.mnemos.social.application.comment.telegram.CreateTelegramCommentCommand
import com.mnemos.social.application.comment.telegram.TelegramCommentCreationResult
import com.mnemos.social.contracts.internal.v1.InternalTelegramCreateCommentRequest
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

class InternalTelegramCommentMutationControllerTest {
    private val actors = mock<ResolveTelegramSocialActorUseCase>()
    private val create = mock<CreateTelegramCommentUseCase>()
    private val controller = InternalTelegramCommentMutationController(actors, create)
    private val actor = SocialActor(UUID.randomUUID(), UUID.randomUUID(), "Kirito")
    private val target = UUID.randomUUID()
    private val operationId = UUID.randomUUID()

    @Test
    fun verifiedActorAndAccountAreBoundToCreatedComment() {
        whenever(actors.resolve(11L)).thenReturn(actor)
        val commentId = UUID.randomUUID()
        whenever(create.execute(CreateTelegramCommentCommand(
            11L, actor, operationId, target, null, "hello",
        ))).thenReturn(TelegramCommentCreationResult.Created(commentId))

        assertEquals(commentId, controller.create(11L, target, request("hello")).commentId)
        verify(create).execute(CreateTelegramCommentCommand(11L, actor, operationId, target, null, "hello"))
    }

    @Test
    fun mismatchedActorActiveAccountNeverCreatesComment() {
        whenever(actors.resolve(11L)).thenReturn(actor)
        val wrong = request("hello").copy(expectedActorAccountId = UUID.randomUUID())

        val response = assertThrows(ResponseStatusException::class.java) {
            controller.create(11L, target, wrong)
        }
        assertEquals(409, response.statusCode.value())
        verifyNoInteractions(create)
    }

    @Test
    fun missingTelegramIdentityNeverCreatesComment() {
        val response = assertThrows(ResponseStatusException::class.java) {
            controller.create(11L, target, request("hello"))
        }
        assertEquals(403, response.statusCode.value())
        verifyNoInteractions(create)
    }

    @Test
    fun sameOperationIdWithChangedBodyIsConflict() {
        whenever(actors.resolve(11L)).thenReturn(actor)
        whenever(create.execute(CreateTelegramCommentCommand(
            11L, actor, operationId, target, null, "changed",
        ))).thenReturn(TelegramCommentCreationResult.Conflict)

        val response = assertThrows(ResponseStatusException::class.java) {
            controller.create(11L, target, request("changed"))
        }
        assertEquals(409, response.statusCode.value())
    }

    @Test
    fun rejectsInvalidCommentLengthBeforeResolvingIdentity() {
        assertThrows(ResponseStatusException::class.java) {
            controller.create(11L, target, request(" "))
        }
        verifyNoInteractions(actors, create)
    }

    @Test
    fun endpointAcceptsOnlyAuthenticatedTelegramServicePrincipal() {
        val guard = InternalTelegramCommentMutationController::class.java.getAnnotation(PreAuthorize::class.java)
        assertNotNull(guard)
        assertEquals("authentication.name == 'telegram-bot-service'", guard.value)
    }

    private fun request(content: String) = InternalTelegramCreateCommentRequest(
        operationId, actor.accountId, null, content,
    )
}
