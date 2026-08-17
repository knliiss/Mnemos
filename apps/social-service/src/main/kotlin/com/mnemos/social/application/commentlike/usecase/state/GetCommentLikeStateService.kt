package com.mnemos.social.application.commentlike.usecase.state

import com.mnemos.social.application.actor.SocialActorProvider
import com.mnemos.social.domain.comment.model.CommentId
import com.mnemos.social.domain.comment.repository.CommentRepository
import com.mnemos.social.domain.reaction.repository.CommentLikeRepository
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.server.ResponseStatusException
import java.util.UUID

@Service
class GetCommentLikeStateService(
    private val repository: CommentLikeRepository,
    private val commentRepository: CommentRepository,
    private val actorProvider: SocialActorProvider,
) : GetCommentLikeStateUseCase {
    @Transactional(readOnly = true)
    override fun execute(commentId: UUID): Boolean {
        val actor = actorProvider.requireActor()
        val comment =
            commentRepository.findById(CommentId(commentId))
                ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Comment was not found")

        if (comment.deleted) {
            return false
        }

        return repository.exists(
            commentId = commentId,
            actorAccountId = actor.accountId,
        )
    }
}
