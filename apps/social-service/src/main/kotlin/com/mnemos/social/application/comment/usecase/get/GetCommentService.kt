package com.mnemos.social.application.comment.usecase.get

import com.mnemos.social.application.comment.exception.CommentNotFoundException
import com.mnemos.social.application.comment.model.CommentView
import com.mnemos.social.application.comment.port.out.CommentViewReader
import com.mnemos.social.domain.comment.model.CommentId
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
class GetCommentService(
    private val reader: CommentViewReader,
) : GetCommentUseCase {
    @Transactional(readOnly = true)
    override fun execute(commentId: UUID): CommentView {
        val id = CommentId(commentId)

        return reader.findById(id)
            ?: throw CommentNotFoundException(id)
    }
}
