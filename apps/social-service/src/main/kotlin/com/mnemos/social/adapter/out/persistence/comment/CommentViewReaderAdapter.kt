package com.mnemos.social.adapter.out.persistence.comment

import com.mnemos.social.adapter.out.persistence.comment.CommentViewPersistenceMapper.toView
import com.mnemos.social.adapter.out.persistence.common.pagination.PagePersistenceMapper.toPageResult
import com.mnemos.social.application.comment.model.CommentView
import com.mnemos.social.application.comment.port.out.CommentViewReader
import com.mnemos.social.application.comment.usecase.list.ListCommentRepliesQuery
import com.mnemos.social.application.comment.usecase.list.ListRootCommentsQuery
import com.mnemos.social.application.common.pagination.PageResult
import com.mnemos.social.domain.comment.model.CommentId
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Component

@Component
class CommentViewReaderAdapter(
    private val repository: CommentViewJpaRepository,
) : CommentViewReader {
    override fun findById(id: CommentId): CommentView? = repository.findViewById(id.id)?.toView()

    override fun findRoot(query: ListRootCommentsQuery): PageResult<CommentView> =
        repository
            .findRootByAccountId(
                targetAccountId = query.accountId,
                pageable = PageRequest.of(query.page.page, query.page.size),
            ).toPageResult { projection -> projection.toView() }

    override fun findReplies(query: ListCommentRepliesQuery): PageResult<CommentView> =
        repository
            .findReplies(
                parentCommentId = query.commentId.id,
                pageable = PageRequest.of(query.page.page, query.page.size),
            ).toPageResult { projection -> projection.toView() }
}
