package com.mnemos.social.application.comment.telegram

import com.mnemos.social.application.comment.usecase.create.CreateCommentCommand
import com.mnemos.social.application.comment.usecase.create.CreateCommentForActorUseCase
import com.mnemos.social.domain.comment.model.CommentId
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import java.util.HexFormat

@Service
class CreateTelegramCommentService(
    private val receipts: TelegramCommentReceiptPort,
    private val comments: CreateCommentForActorUseCase,
) : CreateTelegramCommentUseCase {
    @Transactional
    override fun execute(command: CreateTelegramCommentCommand): TelegramCommentCreationResult {
        require(command.telegramId > 0)
        val content = command.content.trim()
        require(content.isNotBlank() && content.length <= 500)

        val fingerprint = HexFormat.of().formatHex(
            MessageDigest.getInstance("SHA-256").digest(
                listOf(
                    "telegram-comment-v1",
                    command.targetAccountId.toString(),
                    command.parentCommentId?.toString() ?: "-",
                    content,
                ).joinToString("\u0000").toByteArray(StandardCharsets.UTF_8),
            ),
        )

        return when (
            val claim = receipts.claim(
                command.operationId,
                command.telegramId,
                command.actor,
                command.targetAccountId,
                fingerprint,
            )
        ) {
            TelegramCommentClaim.Conflict -> TelegramCommentCreationResult.Conflict
            is TelegramCommentClaim.Replayed -> TelegramCommentCreationResult.Created(claim.commentId)
            TelegramCommentClaim.Claimed -> {
                val comment = comments.executeForActor(
                    CreateCommentCommand(
                        targetAccountId = command.targetAccountId,
                        parentCommentId = command.parentCommentId?.let(::CommentId),
                        content = content,
                    ),
                    command.actor,
                )
                receipts.complete(command.operationId, comment.id.id)
                TelegramCommentCreationResult.Created(comment.id.id)
            }
        }
    }
}
