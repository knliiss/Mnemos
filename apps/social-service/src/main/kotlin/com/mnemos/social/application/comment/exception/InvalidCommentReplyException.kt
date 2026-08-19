package com.mnemos.social.application.comment.exception

import com.mnemos.common.web.exception.AppException
import com.mnemos.social.domain.comment.model.CommentId
import org.springframework.http.HttpStatus
import java.util.UUID

class InvalidCommentReplyException(
    parentCommentId: CommentId,
    targetAccountId: UUID,
    reason: String,
) : AppException(
        message = reason,
        code = "INVALID_COMMENT_REPLY",
        httpStatus = HttpStatus.BAD_REQUEST,
        details = mapOf(
            "parentCommentId" to parentCommentId.id,
            "targetAccountId" to targetAccountId,
        ),
    )
