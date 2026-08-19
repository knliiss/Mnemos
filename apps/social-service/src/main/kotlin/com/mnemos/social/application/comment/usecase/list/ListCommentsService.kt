package com.mnemos.social.application.comment.usecase.list

import com.mnemos.social.application.comment.exception.CommentNotFoundException
import com.mnemos.social.application.comment.exception.CommentRepliesRequireRootException
import com.mnemos.social.application.comment.model.CommentView
import com.mnemos.social.application.comment.port.out.CommentViewReader
import com.mnemos.social.application.common.pagination.PageResult
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class ListCommentsService(
    private val reader: CommentViewReader,
) : ListCommentsUseCase {
    @Transactional(readOnly = true)
    override fun listRoot(query: ListRootCommentsQuery): PageResult<CommentView> = reader.findRoot(query)

    @Transactional(readOnly = true)
    override fun listReplies(query: ListCommentRepliesQuery): PageResult<CommentView> {
        val root = reader.findById(query.commentId)
            ?: throw CommentNotFoundException(query.commentId)

        if (root.parentCommentId != null) {
            throw CommentRepliesRequireRootException(query.commentId)
        }

        return reader.findReplies(query)
    }
}
