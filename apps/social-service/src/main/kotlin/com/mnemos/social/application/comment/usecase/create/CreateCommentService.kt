package com.mnemos.social.application.comment.usecase.create

import com.mnemos.common.core.model.permission.RolePermission
import com.mnemos.social.application.actor.SocialActorProvider
import com.mnemos.social.application.actor.port.out.AccountAccessPort
import com.mnemos.social.application.summary.AccountSocialSummaryService
import com.mnemos.social.domain.comment.model.Comment
import com.mnemos.social.domain.comment.model.CommentId
import com.mnemos.social.domain.comment.repository.CommentRepository
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.server.ResponseStatusException
import java.time.Clock
import java.util.UUID

@Service
class CreateCommentService(
    private val repository: CommentRepository,
    private val actorProvider: SocialActorProvider,
    private val accountAccessPort: AccountAccessPort,
    private val summaryService: AccountSocialSummaryService,
    private val clock: Clock,
) : CreateCommentUseCase {
    @Transactional
    override fun execute(
        targetAccountId: UUID,
        parentCommentId: UUID?,
        content: String,
    ): Comment {
        val actor = actorProvider.requireActor(RolePermission.PUBLISH_COMMENTS)

        if (!accountAccessPort.isActiveAccount(targetAccountId)) {
            throw ResponseStatusException(HttpStatus.NOT_FOUND, "Target account was not found")
        }

        val parent =
            parentCommentId?.let { id ->
                repository.findByIdForUpdate(CommentId(id))
                    ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Comment was not found")
            }

        if (parent != null) {
            if (parent.deleted) {
                throw ResponseStatusException(HttpStatus.CONFLICT, "Deleted comment cannot receive replies")
            }
            if (parent.targetAccountId != targetAccountId) {
                throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Reply must target the same account as its parent")
            }
            if (parent.parentCommentId != null) {
                throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Replies can only target root comments")
            }
        }

        val now = clock.instant()
        val saved =
            repository.save(
                Comment.create(
                    targetAccountId = targetAccountId,
                    authorUserId = actor.userId,
                    authorAccountId = actor.accountId,
                    parentCommentId = parent?.id,
                    content = content,
                    now = now,
                ),
            )

        summaryService.commentAdded(
            accountId = targetAccountId,
            actorUserId = actor.userId,
            now = now,
        )

        return saved
    }
}
