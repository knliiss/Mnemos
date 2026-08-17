package com.mnemos.social.application.comment.usecase.list

import com.mnemos.social.application.comment.model.CommentView
import com.mnemos.social.application.comment.port.out.CommentViewReader
import com.mnemos.social.domain.comment.model.CommentId
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.server.ResponseStatusException
import java.util.UUID

@Service
class ListCommentsService(
    private val reader: CommentViewReader,
) : ListCommentsUseCase {
    @Transactional(readOnly = true)
    override fun listRoot(
        accountId: UUID,
        pageable: Pageable,
    ): Page<CommentView> = reader.findRootByAccountId(accountId, pageable)

    @Transactional(readOnly = true)
    override fun listReplies(
        commentId: UUID,
        pageable: Pageable,
    ): Page<CommentView> {
        val root =
            reader.findById(CommentId(commentId))
                ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Comment was not found")

        if (root.parentCommentId != null) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Replies can only be listed for a root comment")
        }

        return reader.findReplies(root.id, pageable)
    }
}
