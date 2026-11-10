package com.mnemos.social.adapter.inbound.http.comment

import com.mnemos.social.application.comment.model.CommentView
import com.mnemos.social.application.actor.ResolveTelegramSocialActorUseCase
import com.mnemos.social.application.actor.model.SocialActor
import com.mnemos.social.application.actor.port.out.AccountAccessPort
import com.mnemos.social.application.comment.usecase.get.GetCommentUseCase
import com.mnemos.social.application.comment.usecase.list.ListCommentRepliesQuery
import com.mnemos.social.application.comment.usecase.list.ListCommentsUseCase
import com.mnemos.social.application.comment.usecase.list.ListRootCommentsQuery
import com.mnemos.social.application.common.pagination.PageQuery
import com.mnemos.social.application.common.pagination.PageResult
import com.mnemos.social.domain.comment.model.CommentId
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoInteractions
import org.mockito.kotlin.whenever
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.server.ResponseStatusException
import java.time.Instant
import java.util.UUID

class InternalTelegramCommentsControllerTest {
    private val comments = mock<ListCommentsUseCase>()
    private val getComment = mock<GetCommentUseCase>()
    private val actors = mock<ResolveTelegramSocialActorUseCase>()
    private val access = mock<AccountAccessPort>()
    private val controller = InternalTelegramCommentsController(comments, getComment, actors, access)
    private val accountId = UUID.randomUUID()
    private val rootId = UUID.randomUUID()

    @Test
    fun returnsOnlyPublicCommentDataAndHidesDeletedContent() {
        val deleted = view(deleted = true, content = "Private content")
        whenever(comments.listRoot(ListRootCommentsQuery(accountId, PageQuery(0, 3))))
            .thenReturn(PageResult(listOf(deleted), 0, 3, 1, 1, false))

        val result = controller.list(accountId, 0, 3)

        assertEquals(1, result.totalItems)
        assertNull(result.items.single().content)
        assertEquals(deleted.authorAccountId, result.items.single().authorAccountId)
        assertEquals(1, result.items.size)
        assertFalse(result.items.single().javaClass.declaredFields.any { it.name == "authorUserId" })
        assertFalse(result.items.single().javaClass.declaredFields.any { it.name == "canEdit" })
    }

    @Test
    fun preventsReadingRepliesOfUnrelatedAccount() {
        whenever(getComment.execute(rootId)).thenReturn(view(account = UUID.randomUUID()))

        val exception = assertThrows(ResponseStatusException::class.java) {
            controller.replies(accountId, rootId, 0, 3)
        }
        assertEquals(404, exception.statusCode.value())
        verifyNoInteractions(comments)
    }

    @Test
    fun preventsSelectingNestedCommentAsRootThread() {
        whenever(getComment.execute(rootId)).thenReturn(view(parent = CommentId(UUID.randomUUID())))

        assertThrows(ResponseStatusException::class.java) {
            controller.replies(accountId, rootId, 0, 3)
        }
        verifyNoInteractions(comments)
    }

    @Test
    fun existingRootThreadUsesValidatedAccountAndPagination() {
        whenever(getComment.execute(rootId)).thenReturn(view())
        whenever(comments.listReplies(ListCommentRepliesQuery(CommentId(rootId), PageQuery(1, 3))))
            .thenReturn(PageResult(emptyList(), 1, 3, 5, 2, false))

        val result = controller.replies(accountId, rootId, 1, 3)
        assertEquals(5, result.totalItems)
        verify(comments).listReplies(ListCommentRepliesQuery(CommentId(rootId), PageQuery(1, 3)))
    }

    @Test
    fun unlinkedTelegramUserNeverReceivesAuthorPermissions() {
        val comment = view(authorUserId = null)
        whenever(comments.listRoot(ListRootCommentsQuery(accountId, PageQuery(0, 3))))
            .thenReturn(PageResult(listOf(comment), 0, 3, 1, 1, false))

        val result = controller.list(accountId, 0, 3)
        assertFalse(result.items.single().canEdit)
        assertFalse(result.items.single().canDelete)
    }

    @Test
    fun matchedTelegramUserReceivesAuthorControlsWithoutRawUserId() {
        val userId = UUID.randomUUID()
        val actor = SocialActor(userId, UUID.randomUUID(), "Kirito")
        whenever(actors.resolve(11L)).thenReturn(actor)
        whenever(comments.listRoot(ListRootCommentsQuery(accountId, PageQuery(0, 3))))
            .thenReturn(PageResult(listOf(view(authorUserId = userId)), 0, 3, 1, 1, false))

        val result = controller.list(accountId, 0, 3, telegramId = 11L)
        assertTrue(result.items.single().canEdit)
        assertTrue(result.items.single().canDelete)
        assertFalse(result.items.single().javaClass.declaredFields.any { it.name == "authorUserId" })
    }

    @Test
    fun onlyAuthenticatedBotServiceCanCallInternalEndpoint() {
        val guard = InternalTelegramCommentsController::class.java.getAnnotation(PreAuthorize::class.java)
        assertNotNull(guard)
        assertEquals("authentication.name == 'telegram-bot-service'", guard.value)
    }

    private fun view(
        account: UUID = accountId,
        parent: CommentId? = null,
        deleted: Boolean = false,
        content: String? = "Test",
        authorUserId: UUID? = UUID.randomUUID(),
    ) = CommentView(
        id = CommentId(rootId),
        targetAccountId = account,
        authorAccountId = UUID.randomUUID(),
        parentCommentId = parent,
        content = content,
        deleted = deleted,
        likes = 2,
        replies = 1,
        createdAt = Instant.parse("2026-11-10T15:00:00Z"),
        updatedAt = Instant.parse("2026-11-10T15:00:00Z"),
        authorUserId = authorUserId,
    )
}
