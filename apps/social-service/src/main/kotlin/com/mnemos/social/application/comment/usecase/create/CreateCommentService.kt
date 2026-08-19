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
            if (parent.deleted) {
                throw DeletedCommentOperationException(
                    commentId = parent.id,
                    operation = "replied to",
                )
            }

            if (parent.targetAccountId != command.targetAccountId) {
                throw InvalidCommentReplyException(
                    parentCommentId = parent.id,
                    targetAccountId = command.targetAccountId,
                    reason = "Reply must target the same account as its parent",
                )
            }

            if (parent.parentCommentId != null) {
                throw InvalidCommentReplyException(
                    parentCommentId = parent.id,
                    targetAccountId = command.targetAccountId,
                    reason = "Replies can only target root comments",
                )
            }
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
                ),
            )

        summaryService.commentAdded(
            accountId = command.targetAccountId,
            actorUserId = actor.userId,
            now = now,
        )

        return saved
    }
}
