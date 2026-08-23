package com.mnemos.social.application.comment.exception

import com.mnemos.common.web.exception.AppException
import com.mnemos.social.domain.comment.model.CommentId
import org.springframework.http.HttpStatus

class CommentRepliesRequireRootException(
    commentId: CommentId,
) : AppException(
        message = "Replies can only be listed for a root comment",
        code = "COMMENT_REPLIES_REQUIRE_ROOT",
        httpStatus = HttpStatus.BAD_REQUEST,
        details = mapOf("commentId" to commentId.id),
    )
