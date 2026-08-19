package com.mnemos.social.application.comment.usecase.list

import com.mnemos.social.application.common.pagination.PageQuery
import com.mnemos.social.domain.comment.model.CommentId
import java.util.UUID

data class ListRootCommentsQuery(
    val accountId: UUID,
    val page: PageQuery,
)

data class ListCommentRepliesQuery(
    val commentId: CommentId,
    val page: PageQuery,
)
