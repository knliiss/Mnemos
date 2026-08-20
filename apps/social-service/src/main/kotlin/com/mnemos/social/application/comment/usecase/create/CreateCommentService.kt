package com.mnemos.social.application.comment.usecase.create

import com.mnemos.common.core.model.permission.RolePermission
import com.mnemos.social.application.account.exception.SocialAccountNotFoundException
import com.mnemos.social.application.actor.SocialActorProvider
import com.mnemos.social.application.actor.port.out.AccountAccessPort
import com.mnemos.social.application.comment.exception.CommentNotFoundException
import com.mnemos.social.application.comment.exception.DeletedCommentOperationException
import com.mnemos.social.application.comment.exception.InvalidCommentReplyException
import com.mnemos.social.application.summary.AccountSocialSummaryService
import com.mnemos.social.domain.comment.model.Comment
import com.mnemos.social.domain.comment.repository.CommentRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Clock

@Service
class CreateCommentService(
    private val repository: CommentRepository,
    private val actorProvider: SocialActorProvider,
    private val accountAccessPort: AccountAccessPort,
    private val summaryService: AccountSocialSummaryService,
    private val clock: Clock,
) : CreateCommentUseCase {
    @Transactional
    override fun execute(command: CreateCommentCommand): Comment {
        val actor = actorProvider.requireActor(RolePermission.PUBLISH_COMMENTS)

        if (!accountAccessPort.isActiveAccount(command.targetAccountId)) {
            throw SocialAccountNotFoundException(command.targetAccountId)
        }

        val parent =
            command.parentCommentId?.let { id ->
                repository.findByIdForUpdate(id)
                    ?: throw CommentNotFoundException(id)
            }

        if (parent != null) {
            validateRootParent(
                parent = parent,
                targetAccountId = command.targetAccountId,
            )
        } else if (command.replyToCommentId != null) {
            throw InvalidCommentReplyException(
                parentCommentId = command.replyToCommentId,
                targetAccountId = command.targetAccountId,
                reason = "Reply target requires a root parent comment",
            )
        }

        val replyTarget =
            command.replyToCommentId?.let { id ->
                if (parent?.id == id) {
                    parent
                } else {
                    repository.findByIdForUpdate(id)
                        ?: throw CommentNotFoundException(id)
                }
            }

        if (replyTarget != null && parent != null) {
            validateReplyTarget(
                parent = parent,
                replyTarget = replyTarget,
                targetAccountId = command.targetAccountId,
            )
        }

        val now = clock.instant()
        val saved =
            repository.save(
                Comment.create(
                    targetAccountId = command.targetAccountId,
                    authorUserId = actor.userId,
                    authorAccountId = actor.accountId,
                    parentCommentId = parent?.id,
                    content = command.content,
                    now = now,
                    replyToCommentId = replyTarget?.id,
                ),
            )

        summaryService.commentAdded(
            accountId = command.targetAccountId,
            actorUserId = actor.userId,
            now = now,
        )

        return saved
    }

    private fun validateRootParent(
        parent: Comment,
        targetAccountId: java.util.UUID,
    ) {
        if (parent.deleted) {
            throw DeletedCommentOperationException(
                commentId = parent.id,
                operation = "replied to",
            )
        }

        if (parent.targetAccountId != targetAccountId) {
            throw InvalidCommentReplyException(
                parentCommentId = parent.id,
                targetAccountId = targetAccountId,
                reason = "Reply must target the same account as its parent",
            )
        }

        if (parent.parentCommentId != null) {
            throw InvalidCommentReplyException(
                parentCommentId = parent.id,
                targetAccountId = targetAccountId,
                reason = "parentCommentId must reference the root comment",
            )
        }
    }

    private fun validateReplyTarget(
        parent: Comment,
        replyTarget: Comment,
        targetAccountId: java.util.UUID,
    ) {
        if (replyTarget.deleted) {
            throw DeletedCommentOperationException(
                commentId = replyTarget.id,
                operation = "replied to",
            )
        }

        if (replyTarget.targetAccountId != targetAccountId) {
            throw InvalidCommentReplyException(
                parentCommentId = parent.id,
                targetAccountId = targetAccountId,
                reason = "Reply target must belong to the same account",
            )
        }

        val belongsToThread = replyTarget.id == parent.id || replyTarget.parentCommentId == parent.id

        if (!belongsToThread) {
            throw InvalidCommentReplyException(
                parentCommentId = parent.id,
                targetAccountId = targetAccountId,
                reason = "Reply target must belong to the same root thread",
            )
        }
    }
}
