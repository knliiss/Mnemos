package com.mnemos.social.application.comment.usecase.delete

import com.mnemos.common.core.model.permission.RolePermission
import com.mnemos.common.core.model.user.UserRole
import com.mnemos.social.application.actor.SocialActorProvider
import com.mnemos.social.application.actor.model.SocialActor
import com.mnemos.social.application.actor.port.out.AccountAccessPort
import com.mnemos.social.application.comment.exception.CommentAccessDeniedException
import com.mnemos.social.application.comment.exception.CommentNotFoundException
import com.mnemos.social.application.summary.AccountSocialSummaryService
import com.mnemos.social.domain.comment.model.CommentId
import com.mnemos.social.domain.comment.repository.CommentRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Clock
import java.util.UUID

@Service
class DeleteCommentService(
    private val repository: CommentRepository,
    private val actorProvider: SocialActorProvider,
    private val summaryService: AccountSocialSummaryService,
    private val accountAccess: AccountAccessPort,
    private val clock: Clock,
) : DeleteCommentUseCase, DeleteCommentForActorUseCase {
    @Transactional
    override fun execute(commentId: UUID) {
        delete(commentId, actorProvider.requireUserId(), globalAdmin = actorProvider.hasAnyUserRole(
            UserRole.ADMIN, UserRole.SUPER_ADMIN,
        ), accountModerator = { accountId, userId ->
            actorProvider.hasPermission(accountId, userId, RolePermission.MODERATE_COMMENTS)
        })
    }

    @Transactional
    override fun executeForActor(commentId: UUID, actor: SocialActor) {
        delete(commentId, actor.userId, globalAdmin = false, accountModerator = { accountId, userId ->
            accountAccess.hasPermission(accountId, userId, RolePermission.MODERATE_COMMENTS)
        })
    }

    private fun delete(
        commentId: UUID,
        userId: UUID,
        globalAdmin: Boolean,
        accountModerator: (UUID, UUID) -> Boolean,
    ) {
        val id = CommentId(commentId)
        val preview =
            repository.findById(id)
                ?: throw CommentNotFoundException(id)
        val isAuthor = preview.authorUserId == userId
        val isGlobalAdmin = globalAdmin
        val canModerateAccount =
            !isAuthor &&
                !isGlobalAdmin &&
                accountModerator(preview.targetAccountId, userId)

        if (!isAuthor && !isGlobalAdmin && !canModerateAccount) {
            throw CommentAccessDeniedException(id)
        }

        val comment =
            repository.findByIdForUpdate(id)
                ?: throw CommentNotFoundException(id)

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
