package com.mnemos.social.adapter.inbound.http.comment

import com.mnemos.social.adapter.inbound.http.common.pagination.PageResponse
import com.mnemos.social.adapter.inbound.http.common.pagination.PageResponseMapper.toPageResponse
import com.mnemos.social.application.comment.model.CommentView
import com.mnemos.social.application.comment.usecase.create.CreateCommentCommand
import com.mnemos.social.application.comment.usecase.edit.EditCommentCommand
import com.mnemos.social.application.comment.usecase.list.ListCommentRepliesQuery
import com.mnemos.social.application.comment.usecase.list.ListRootCommentsQuery
import com.mnemos.social.application.common.pagination.PageQuery
import com.mnemos.social.application.common.pagination.PageResult
import com.mnemos.social.domain.comment.model.CommentId
import java.util.UUID

object CommentApiMapper {
    fun CreateCommentRequest.toCommand(accountId: UUID): CreateCommentCommand =
        CreateCommentCommand(
            targetAccountId = accountId,
            parentCommentId = parentCommentId?.let(::CommentId),
            content = content,
        )

    fun EditCommentRequest.toCommand(commentId: UUID): EditCommentCommand =
        EditCommentCommand(
            commentId = CommentId(commentId),
            content = content,
        )

    fun ListCommentsRequest.toRootQuery(accountId: UUID): ListRootCommentsQuery =
        ListRootCommentsQuery(
            accountId = accountId,
            page = toPageQuery(),
        )

    fun ListCommentsRequest.toRepliesQuery(commentId: UUID): ListCommentRepliesQuery =
        ListCommentRepliesQuery(
            commentId = CommentId(commentId),
            page = toPageQuery(),
        )

    fun PageResult<CommentView>.toResponse(): PageResponse<CommentResponse> = toPageResponse { view -> view.toResponse() }

    fun CommentView.toResponse(): CommentResponse =
        CommentResponse(
            id = id.id,
            targetAccountId = targetAccountId,
            authorAccountId = authorAccountId,
            parentCommentId = parentCommentId?.id,
            content = content,
            deleted = deleted,
            likes = likes,
            replies = replies,
            createdAt = createdAt,
            updatedAt = updatedAt,
        )

    private fun ListCommentsRequest.toPageQuery(): PageQuery =
        PageQuery(
            page = page,
            size = size,
        )
}
