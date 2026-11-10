package com.mnemos.social.application.comment.telegram

import com.mnemos.social.application.actor.model.SocialActor
import com.mnemos.social.application.comment.usecase.create.CreateCommentCommand
import com.mnemos.social.application.comment.usecase.create.CreateCommentForActorUseCase
import com.mnemos.social.domain.comment.model.Comment
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoInteractions
import org.mockito.kotlin.whenever
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import java.util.UUID

class CreateTelegramCommentServiceTest {
    private val receipts = mock<TelegramCommentReceiptPort>()
    private val comments = mock<CreateCommentForActorUseCase>()
    private val service = CreateTelegramCommentService(receipts, comments)
    private val actor = SocialActor(UUID.randomUUID(), UUID.randomUUID(), "Kirito")
    private val operationId = UUID.randomUUID()
    private val target = UUID.randomUUID()

    @Test
    fun newOperationCreatesOneCommentAndRecordsItsIdInSameTransaction() {
        val request = command(" Вітаю ")
        val created = Comment.create(target, actor.userId, actor.accountId, null, "Вітаю", Instant.EPOCH)
        whenever(receipts.claim(eq(operationId), eq(11L), eq(actor), eq(target), any()))
            .thenReturn(TelegramCommentClaim.Claimed)
        whenever(comments.executeForActor(CreateCommentCommand(target, null, "Вітаю"), actor))
            .thenReturn(created)

        assertEquals(TelegramCommentCreationResult.Created(created.id.id), service.execute(request))
        verify(receipts).complete(operationId, created.id.id)
        verify(comments).executeForActor(CreateCommentCommand(target, null, "Вітаю"), actor)
        assertTrue(CreateTelegramCommentService::class.java.getMethod("execute", CreateTelegramCommentCommand::class.java)
            .isAnnotationPresent(Transactional::class.java))
    }

    @Test
    fun duplicateDeliveryReturnsOriginalCommentWithoutWritingAgain() {
        val priorId = UUID.randomUUID()
        whenever(receipts.claim(eq(operationId), eq(11L), eq(actor), eq(target), any()))
            .thenReturn(TelegramCommentClaim.Replayed(priorId))

        assertEquals(TelegramCommentCreationResult.Created(priorId), service.execute(command("Test")))
        verifyNoInteractions(comments)
    }

    @Test
    fun reusedOperationIdWithDifferentPayloadFailsClosed() {
        whenever(receipts.claim(eq(operationId), eq(11L), eq(actor), eq(target), any()))
            .thenReturn(TelegramCommentClaim.Conflict)

        assertEquals(TelegramCommentCreationResult.Conflict, service.execute(command("Other")))
        verifyNoInteractions(comments)
    }

    @Test
    fun invalidContentNeverClaimsReceipt() {
        assertThrows(IllegalArgumentException::class.java) { service.execute(command(" ")) }
        assertThrows(IllegalArgumentException::class.java) { service.execute(command("a".repeat(501))) }
        verifyNoInteractions(receipts, comments)
    }

    private fun command(content: String) = CreateTelegramCommentCommand(
        telegramId = 11L,
        actor = actor,
        operationId = operationId,
        targetAccountId = target,
        parentCommentId = null,
        content = content,
    )
}
