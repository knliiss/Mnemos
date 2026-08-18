package com.mnemos.social.application.comment.usecase.delete

import com.mnemos.common.core.model.permission.RolePermission
import com.mnemos.common.core.model.user.UserRole
import com.mnemos.social.application.actor.SocialActorProvider
import com.mnemos.social.application.summary.AccountSocialSummaryService
import com.mnemos.social.domain.comment.model.CommentId
import com.mnemos.social.domain.comment.repository.CommentRepository
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.server.ResponseStatusException
import java.time.Clock
import java.util.UUID

@Service
class DeleteCommentService(
    private val repository: CommentRepository,
    private val actorProvider: SocialActorProvider,
    private val summaryService: AccountSocialSummaryService,
    private val clock: Clock,
) : DeleteCommentUseCase {
    @Transactional
    override fun execute(commentId: UUID) {
        val userId = actorProvider.requireUserId()
        val id = CommentId(commentId)
        val preview =
            repository.findById(id)
                ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Comment was not found")
        val isAuthor = preview.authorUserId == userId
        val isGlobalAdmin =
            actorProvider.hasAnyUserRole(
                UserRole.ADMIN,
                UserRole.SUPER_ADMIN,
            )
        val canModerateAccount =
            !isAuthor &&
                !isGlobalAdmin &&
                actorProvider.hasPermission(
                    accountId = preview.targetAccountId,
                    userId = userId,
                    permission = RolePermission.MODERATE_COMMENTS,
                )

        if (!isAuthor && !isGlobalAdmin && !canModerateAccount) {
            throw ResponseStatusException(
                HttpStatus.FORBIDDEN,
                "Comment moderation permission is required",
            )
        }

        val comment =
            repository.findByIdForUpdate(id)
                ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Comment was not found")

        if (comment.deleted) {
            return
        }

        val now = clock.instant()
        comment.delete(now)
        repository.save(comment)

        summaryService.commentRemoved(
            accountId = comment.targetAccountId,
            actorUserId = userId,
            now = now,
        )
    }
}
