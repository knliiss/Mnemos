package com.mnemos.social.application.comment.exception

import com.mnemos.common.web.exception.AppException
import com.mnemos.social.domain.comment.model.CommentId
import org.springframework.http.HttpStatus

class DeletedCommentOperationException(
    commentId: CommentId,
    operation: String,
) : AppException(
        message = "Deleted comment cannot be $operation",
        code = "DELETED_COMMENT_OPERATION_NOT_ALLOWED",
        httpStatus = HttpStatus.CONFLICT,
        details =
            mapOf(
                "commentId" to commentId.id,
                "operation" to operation,
            ),
    )
