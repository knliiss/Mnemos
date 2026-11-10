package com.mnemos.social.adapter.inbound.http.comment

import com.mnemos.social.application.actor.ResolveTelegramSocialActorUseCase
import com.mnemos.social.application.actor.model.SocialActor
import com.mnemos.social.application.comment.telegram.CreateTelegramCommentUseCase
import com.mnemos.social.application.comment.telegram.CreateTelegramCommentCommand
import com.mnemos.social.application.comment.telegram.TelegramCommentCreationResult
import com.mnemos.social.application.comment.usecase.get.GetCommentUseCase
import com.mnemos.social.application.comment.usecase.edit.EditCommentCommand
import com.mnemos.social.application.comment.model.CommentView
import com.mnemos.social.domain.comment.model.CommentId
import com.mnemos.social.contracts.internal.v1.InternalTelegramEditCommentRequest
import com.mnemos.social.contracts.internal.v1.InternalTelegramDeleteCommentRequest
import java.time.Instant
import com.mnemos.social.application.comment.usecase.edit.EditCommentForActorUseCase
import com.mnemos.social.application.comment.usecase.delete.DeleteCommentForActorUseCase
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
    private val edit = mock<EditCommentForActorUseCase>()
    private val delete = mock<DeleteCommentForActorUseCase>()
    private val get = mock<GetCommentUseCase>()
    private val controller = InternalTelegramCommentMutationController(actors, create, edit, delete, get)
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
    fun editUsesVerifiedUserAndChecksCommentProfileOwnership() {
        val commentId = UUID.randomUUID()
        whenever(actors.resolve(11L)).thenReturn(actor)
        whenever(get.execute(commentId)).thenReturn(comment(commentId))
        val updated = com.mnemos.social.domain.comment.model.Comment.create(
            target, actor.userId, actor.accountId, null, "Updated", Instant.EPOCH,
        )
        whenever(edit.executeForActor(EditCommentCommand(CommentId(commentId), "Updated"), actor))
            .thenReturn(updated)

        val response = controller.edit(11L, target, commentId, InternalTelegramEditCommentRequest(actor.accountId, " Updated "))
        assertEquals(updated.id.id, response.commentId)
        verify(edit).executeForActor(EditCommentCommand(CommentId(commentId), "Updated"), actor)
    }

    @Test
    fun deletionUsesActorScopedDomainPermissionChecks() {
        val commentId = UUID.randomUUID()
        whenever(actors.resolve(11L)).thenReturn(actor)
        whenever(get.execute(commentId)).thenReturn(comment(commentId))

        controller.delete(11L, target, commentId, InternalTelegramDeleteCommentRequest(actor.accountId))

        verify(delete).executeForActor(commentId, actor)
    }

    @Test
    fun mismatchedCommentProfileCannotEditOrDelete() {
        val commentId = UUID.randomUUID()
        whenever(actors.resolve(11L)).thenReturn(actor)
        whenever(get.execute(commentId)).thenReturn(comment(commentId).copy(targetAccountId = UUID.randomUUID()))
        assertEquals(404, assertThrows(ResponseStatusException::class.java) {
            controller.delete(11L, target, commentId, InternalTelegramDeleteCommentRequest(actor.accountId))
        }.statusCode.value())
        verifyNoInteractions(edit, delete)
    }

    @Test
    fun endpointAcceptsOnlyAuthenticatedTelegramServicePrincipal() {
        val guard = InternalTelegramCommentMutationController::class.java.getAnnotation(PreAuthorize::class.java)
        assertNotNull(guard)
        assertEquals("authentication.name == 'telegram-bot-service'", guard.value)
    }

    private fun comment(id: UUID): CommentView = CommentView(
        id = CommentId(id),
        targetAccountId = target,
        authorAccountId = actor.accountId,
        parentCommentId = null,
        content = "hello",
        deleted = false,
        likes = 0,
        replies = 0,
        createdAt = Instant.EPOCH,
        updatedAt = Instant.EPOCH,
        authorUserId = actor.userId,
    )

    private fun request(content: String) = InternalTelegramCreateCommentRequest(
        operationId, actor.accountId, null, content,
    )
}
