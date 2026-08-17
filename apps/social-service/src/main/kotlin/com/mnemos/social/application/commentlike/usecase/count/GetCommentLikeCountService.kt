package com.mnemos.social.application.commentlike.usecase.count

import com.mnemos.social.domain.reaction.repository.CommentLikeRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
class GetCommentLikeCountService(
    private val repository: CommentLikeRepository,
) : GetCommentLikeCountUseCase {
    @Transactional(readOnly = true)
    override fun execute(commentId: UUID): Long = repository.countByCommentId(commentId)
}
