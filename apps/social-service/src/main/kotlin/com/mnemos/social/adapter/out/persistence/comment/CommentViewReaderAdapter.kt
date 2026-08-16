package com.mnemos.social.adapter.out.persistence.comment

import com.mnemos.social.adapter.out.persistence.comment.CommentViewPersistenceMapper.toView
import com.mnemos.social.application.comment.model.CommentView
import com.mnemos.social.application.comment.port.out.CommentViewReader
import com.mnemos.social.domain.comment.model.CommentId
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Component
import java.util.UUID

@Component
class CommentViewReaderAdapter(
    private val repository: CommentViewJpaRepository,
) : CommentViewReader {
    override fun findById(id: CommentId): CommentView? = repository.findViewById(id.id)?.toView()

    override fun findRootByAccountId(
        accountId: UUID,
        pageable: Pageable,
    ): Page<CommentView> =
        repository
            .findRootByAccountId(accountId, pageable)
            .map { projection -> projection.toView() }

    override fun findReplies(
        parentCommentId: CommentId,
        pageable: Pageable,
    ): Page<CommentView> =
        repository
            .findReplies(parentCommentId.id, pageable)
            .map { projection -> projection.toView() }
}
