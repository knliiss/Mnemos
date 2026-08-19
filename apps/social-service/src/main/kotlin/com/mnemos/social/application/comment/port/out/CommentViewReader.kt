package com.mnemos.social.application.comment.port.out

import com.mnemos.social.application.comment.model.CommentView
import com.mnemos.social.application.comment.usecase.list.ListCommentRepliesQuery
import com.mnemos.social.application.comment.usecase.list.ListRootCommentsQuery
import com.mnemos.social.application.common.pagination.PageResult
import com.mnemos.social.domain.comment.model.CommentId

interface CommentViewReader {
    fun findById(id: CommentId): CommentView?

    fun findRoot(query: ListRootCommentsQuery): PageResult<CommentView>

    fun findReplies(query: ListCommentRepliesQuery): PageResult<CommentView>
}
