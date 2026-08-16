package com.mnemos.social.application.comment

import com.mnemos.common.core.model.permission.RolePermission
import com.mnemos.social.application.actor.SocialActorProvider
import com.mnemos.social.application.actor.port.out.AccountAccessPort
import com.mnemos.social.application.summary.AccountSocialSummaryService
import com.mnemos.social.domain.comment.model.Comment
import com.mnemos.social.domain.comment.model.CommentId
import com.mnemos.social.domain.comment.repository.CommentRepository
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.server.ResponseStatusException
import java.time.Clock
import java.util.UUID

@Service
class CommentService(
    private val repository: CommentRepository,
    private val actorProvider: SocialActorProvider,
    private val accountAccessPort: AccountAccessPort,
    private val summaryService: AccountSocialSummaryService,
    private val clock: Clock,
) {
    @Transactional
    fun create(
        targetAccountId: UUID,
        parentCommentId: UUID?,
        content: String,
    ): Comment {
        val actor = actorProvider.requireActor(RolePermission.PUBLISH_COMMENTS)

        if (!accountAccessPort.isActiveAccount(targetAccountId)) {
            throw ResponseStatusException(HttpStatus.NOT_FOUND, "Target account was not found")
        }

        val parent = parentCommentId?.let { requireComment(CommentId(it)) }

        if (parent != null) {
            if (parent.deleted) {
                throw ResponseStatusException(HttpStatus.CONFLICT, "Deleted comment cannot receive replies")
            }
            require(parent.targetAccountId == targetAccountId) {
                "Reply must target the same account as its parent"
            }
            require(parent.parentCommentId == null) {
                "Replies can only target root comments"
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

    @Transactional
    fun edit(
        commentId: UUID,
        content: String,
    ): Comment {
        val actor = actorProvider.requireActor()
        val comment = requireComment(CommentId(commentId))

        if (comment.authorUserId != actor.userId) {
            throw ResponseStatusException(
                HttpStatus.FORBIDDEN,
                "Only the author user can edit this comment",
            )
        }

        comment.edit(content, clock.instant())

        return repository.save(comment)
    }

    @Transactional
    fun delete(commentId: UUID) {
        val actor = actorProvider.requireActor()
        val comment = requireComment(CommentId(commentId))
        val isAuthor = comment.authorUserId == actor.userId
        val canModerate =
            actorProvider.hasPermission(
                accountId = comment.targetAccountId,
                userId = actor.userId,
                permission = RolePermission.MODERATE_COMMENTS,
            )

        if (!isAuthor && !canModerate) {
            throw ResponseStatusException(
                HttpStatus.FORBIDDEN,
                "Comment moderation permission is required",
            )
        }

        if (comment.deleted) {
            return
        }

        val now = clock.instant()
        comment.delete(now)
        repository.save(comment)

        summaryService.commentRemoved(
            accountId = comment.targetAccountId,
            actorUserId = actor.userId,
            now = now,
        )
    }

    @Transactional(readOnly = true)
    fun listRoot(
        accountId: UUID,
        pageable: Pageable,
    ): Page<Comment> = repository.findRootByAccountId(accountId, pageable)

    @Transactional(readOnly = true)
    fun listReplies(
        commentId: UUID,
        pageable: Pageable,
    ): Page<Comment> {
        val root = requireComment(CommentId(commentId))

        require(root.parentCommentId == null) {
            "Replies can only be listed for a root comment"
        }

        return repository.findReplies(root.id, pageable)
    }

    private fun requireComment(id: CommentId): Comment =
        repository.findById(id)
            ?: throw ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Comment was not found",
            )
}
