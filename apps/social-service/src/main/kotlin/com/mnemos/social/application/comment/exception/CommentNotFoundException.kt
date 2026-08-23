package com.mnemos.social.application.comment.exception

import com.mnemos.common.web.exception.AppException
import com.mnemos.social.domain.comment.model.CommentId
import org.springframework.http.HttpStatus

class CommentNotFoundException(
    commentId: CommentId,
) : AppException(
        message = "Comment was not found",
        code = "COMMENT_NOT_FOUND",
        httpStatus = HttpStatus.NOT_FOUND,
        details = mapOf("commentId" to commentId.id),
    )
