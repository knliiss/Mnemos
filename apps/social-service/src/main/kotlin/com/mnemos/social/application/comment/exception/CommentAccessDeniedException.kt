package com.mnemos.social.application.comment.exception

import com.mnemos.common.web.exception.AppException
import com.mnemos.social.domain.comment.model.CommentId
import org.springframework.http.HttpStatus

class CommentAccessDeniedException(
    commentId: CommentId,
) : AppException(
        message = "Comment access denied",
        code = "COMMENT_ACCESS_DENIED",
        httpStatus = HttpStatus.FORBIDDEN,
        details = mapOf("commentId" to commentId.id),
    )
