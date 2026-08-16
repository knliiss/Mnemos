package com.mnemos.social.application.comment

import com.mnemos.social.application.comment.port.out.CommentViewReader
import com.mnemos.social.application.comment.query.CommentView
import com.mnemos.social.domain.comment.model.CommentId
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.server.ResponseStatusException
import java.util.UUID

@Service
class CommentQueryService(
    private val reader: CommentViewReader,
) {
    @Transactional(readOnly = true)
    fun get(commentId: UUID): CommentView = requireComment(CommentId(commentId))

    @Transactional(readOnly = true)
    fun listRoot(
        accountId: UUID,
        pageable: Pageable,
    ): Page<CommentView> = reader.findRootByAccountId(accountId, pageable)

    @Transactional(readOnly = true)
    fun listReplies(
        commentId: UUID,
        pageable: Pageable,
    ): Page<CommentView> {
        val root = requireComment(CommentId(commentId))

        if (root.parentCommentId != null) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Replies can only be listed for a root comment")
        }

        return reader.findReplies(root.id, pageable)
    }

    private fun requireComment(id: CommentId): CommentView =
        reader.findById(id)
            ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Comment was not found")
}
