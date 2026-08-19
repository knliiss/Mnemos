package com.mnemos.social.application.comment.usecase.list

import com.mnemos.social.application.comment.model.CommentView
import com.mnemos.social.application.common.pagination.PageResult

interface ListCommentsUseCase {
    fun listRoot(query: ListRootCommentsQuery): PageResult<CommentView>

    fun listReplies(query: ListCommentRepliesQuery): PageResult<CommentView>
}
