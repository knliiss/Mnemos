package com.mnemos.social.application.comment.usecase.get

import com.mnemos.social.application.comment.model.CommentView
import com.mnemos.social.application.comment.port.out.CommentViewReader
import com.mnemos.social.domain.comment.model.CommentId
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.server.ResponseStatusException
import java.util.UUID

@Service
class GetCommentService(
    private val reader: CommentViewReader,
) : GetCommentUseCase {
    @Transactional(readOnly = true)
    override fun execute(commentId: UUID): CommentView =
        reader.findById(CommentId(commentId))
            ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Comment was not found")
}
