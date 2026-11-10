package com.mnemos.social.application.comment.usecase.edit

import com.mnemos.social.application.actor.SocialActorProvider
import com.mnemos.social.application.actor.model.SocialActor
import com.mnemos.social.application.comment.exception.CommentAccessDeniedException
import com.mnemos.social.application.comment.exception.CommentNotFoundException
import com.mnemos.social.application.comment.exception.DeletedCommentOperationException
import com.mnemos.social.domain.comment.model.Comment
import com.mnemos.social.domain.comment.repository.CommentRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Clock

@Service
class EditCommentService(
    private val repository: CommentRepository,
    private val actorProvider: SocialActorProvider,
    private val clock: Clock,
) : EditCommentUseCase, EditCommentForActorUseCase {
    @Transactional
    override fun execute(command: EditCommentCommand): Comment =
        edit(command, actorProvider.requireUserId())

    @Transactional
    override fun executeForActor(command: EditCommentCommand, actor: SocialActor): Comment =
        edit(command, actor.userId)

    private fun edit(command: EditCommentCommand, userId: java.util.UUID): Comment {
        val comment =
            repository.findByIdForUpdate(command.commentId)
                ?: throw CommentNotFoundException(command.commentId)

        if (comment.authorUserId != userId) {
            throw CommentAccessDeniedException(command.commentId)
        }

        if (comment.deleted) {
            throw DeletedCommentOperationException(
                commentId = command.commentId,
                operation = "edited",
            )
        }

        comment.edit(command.content, clock.instant())

        return repository.save(comment)
    }
}
