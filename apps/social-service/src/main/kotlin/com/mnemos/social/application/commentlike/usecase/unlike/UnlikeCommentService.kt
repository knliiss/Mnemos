package com.mnemos.social.application.commentlike.usecase.unlike

import com.mnemos.social.application.actor.SocialActorProvider
import com.mnemos.social.domain.reaction.repository.CommentLikeRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
class UnlikeCommentService(
    private val repository: CommentLikeRepository,
    private val actorProvider: SocialActorProvider,
) : UnlikeCommentUseCase {
    @Transactional
    override fun execute(commentId: UUID) {
        val actor = actorProvider.requireActor()

        repository.remove(
            commentId = commentId,
            actorAccountId = actor.accountId,
        )
    }
}
